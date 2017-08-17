/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.streams

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.datastax.driver.core.ResultSet
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.Unspecified
import com.outworkers.phantom.builder.batch.{BatchQuery, BatchType}
import com.outworkers.phantom.builder.query.{Batchable, QueryOptions, UsingPart}
import com.outworkers.phantom.dsl._
import org.reactivestreams.{Subscriber, Subscription}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
 * The [[Subscriber]] internal implementation based on Akka actors.
 *
 * @see [[com.outworkers.phantom.streams.StreamedCassandraTable.subscriber()]]
 */
class BatchSubscriber[CT <: CassandraTable[CT, T], T] private[streams] (
  table: CT,
  builder: RequestBuilder[CT, T],
  batchSize: Int,
  concurrentRequests: Int,
  batchType: BatchType,
  flushInterval: Option[FiniteDuration],
  completionFn: () => Unit,
  errorFn: Throwable => Unit
)(implicit system: ActorSystem, session: Session, space: KeySpace, ev: Manifest[T]) extends Subscriber[T] {

  private[this] var actor: Option[ActorRef] = None

  override def onSubscribe(s: Subscription): Unit = {
    if (Option(s).isEmpty) throw new NullPointerException()

    if (actor.isEmpty) {
      actor = Some(system.actorOf(
        Props(
          new BatchActor(
            table,
            builder,
            s,
            batchSize,
            concurrentRequests,
            batchType,
            flushInterval,
            completionFn,
            errorFn
          )
        ))
      )
      s.request(multiplyExact(batchSize, concurrentRequests))
    } else {
      // rule 2.5, must cancel subscription as onSubscribe has been invoked twice
      // https://github.com/reactive-streams/reactive-streams-jvm#2.5
      s.cancel()
    }
  }

  override def onNext(t: T): Unit = {
    if (Option(t).isEmpty) {
      throw new NullPointerException("onNext should not be called until onSubscribe has returned")
    }
    actor.get ! t
  }

  override def onError(t: Throwable): Unit = {
    if (Option(t).isEmpty) {
      throw new NullPointerException()
    }
    actor.get ! ErrorWrapper(t)
  }

  override def onComplete(): Unit = {
    actor.get ! BatchActor.Completed
  }

}

object BatchActor {
  case object Completed
  case object ForceExecution
}

case class ErrorWrapper(err: Throwable)

class BatchActor[CT <: CassandraTable[CT, T], T](
  table: CT,
  builder: RequestBuilder[CT, T],
  subscription: Subscription,
  batchSize: Int,
  concurrentRequests: Int,
  batchType: BatchType,
  flushInterval: Option[FiniteDuration],
  completionFn: () => Unit,
  errorFn: Throwable => Unit
)(implicit session: Session, space: KeySpace, ev: Manifest[T]) extends Actor {

  import context.{dispatcher, system}

  private[this] val buffer = ArrayBuffer.empty[T]

  buffer.sizeHint(batchSize)

  private[this] var completed = false

  /** It's only created if a flushInterval is provided */
  private[this] val scheduler = flushInterval.map { interval =>
    system.scheduler.schedule(
      interval,
      interval,
      self,
      BatchActor.ForceExecution
    )
  }

  def receive: Receive = {
    case t: ErrorWrapper =>
      handleError(t.err)

    case BatchActor.Completed =>
      if (buffer.nonEmpty) {
        executeStatements()
      }
      completed = true

    case BatchActor.ForceExecution =>
      if (buffer.nonEmpty) {
        executeStatements()
      }

    case rs: ResultSet =>
      if (completed) {
        shutdown()
      } else {
        subscription.request(batchSize)
      }

    case t: T =>
      buffer.append(t)
      if (buffer.size == batchSize) {
        executeStatements()
      }
  }

  // Stops the scheduler if it exists
  override def postStop(): Unit = scheduler.map(_.cancel())

  private[this] def shutdown(): Unit = {
    completionFn()
    context.stop(self)
  }

  private[this] def handleError(t: Throwable): Unit = {
    subscription.cancel()
    errorFn(t)
    buffer.clear()
    context.stop(self)
  }

  private[this] def executeStatements(): Unit = {
    val query = BatchQuery[Unspecified](
      buffer.map(builder.request(table, _)).iterator,
      batchType,
      UsingPart.empty,
      QueryOptions.empty
    )
    query.future().onComplete {
      case Failure(e) => self ! ErrorWrapper(e)
      case Success(resp) => self ! resp
    }
    buffer.clear()
  }

}

/**
 * This is the typeclass that should be implemented for a
 * given instance of T. Every implementation of this typeclass
 * should be provided implicitly in the scope in order to be
 * used by the stream.
 *
 * {{{
 * implicit object MyRequestBuilderForT extends RequestBuilder[CT, T] {
 *  override def request(ct: CT, t: T): ExecutableStatement =
 * ct.insert().value(_.name, t.name)
 * }
 * }}}
 *
 * @tparam CT the concrete [[CassandraTable]] implementation type
 * @tparam T the type of streamed elements
 */
trait RequestBuilder[CT <: CassandraTable[CT, T], T] {
  def request(ct: CT, t: T)(implicit session: Session, keySpace: KeySpace): Batchable
}