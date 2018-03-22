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
package com.outworkers.phantom

import akka.actor.ActorSystem
import com.datastax.driver.core.{Session, Statement}
import com.outworkers.phantom.builder.batch.BatchType
import com.outworkers.phantom.builder.query.prepared.ExecutablePreparedSelectQuery
import com.outworkers.phantom.builder.query.{RootSelectBlock, SelectQuery}
import com.outworkers.phantom.builder.{ConsistencyBound, LimitBound, OrderBound, WhereBound}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.streams.iteratee.{Enumerator, Iteratee => PhantomIteratee}
import com.outworkers.phantom.streams.lib.EnumeratorPublisher
import org.reactivestreams.Publisher
import play.api.libs.iteratee.{Enumeratee, Enumerator => PlayEnumerator}
import shapeless.HList

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration

/**
 * Just a wrapper module for enhancing phantom [[CassandraTable]]
 * with reactive streams features.
 *
 * In order to be used, please be sured to import the implicits
 * into the scope.
 *
 * {{{
 * import ReactiveCassandra._
 * val subscriber = CassandraTableInstance.subscriber()
 * }}}
 *
 * @see [[http://www.reactive-streams.org/]]
 * @see [[https://github.com/websudos/phantom]]
 */
package object streams {

  /**
    * Adapt an Enumerator to a Publisher. Each Subscriber will be
    * adapted to an Iteratee and applied to the Enumerator. Input of
    * type Input.El will result in calls to onNext.
    *
    * Either onError or onComplete will always be invoked as the
    * last call to the subscriber, the former happening if the
    * enumerator fails with an error, the latter happening when
    * the first of either Input.EOF is fed, or the enumerator
    * completes.
    *
    * If emptyElement is None then Input of type Input.Empty will
    * be ignored. If it is set to Some(x) then it will call onNext
    * with the value x.
    */
  def enumeratorToPublisher[T](enum: PlayEnumerator[T], emptyElement: Option[T] = None): Publisher[T] = {
    new EnumeratorPublisher(enum, emptyElement)
  }

  private[this] final val DEFAULT_CONCURRENT_REQUESTS = 5

  private[this] final val DEFAULT_BATCH_SIZE = 100
  /**
   * @tparam CT the concrete type inheriting from [[CassandraTable]]
   * @tparam T the type of the streamed element
   */
  implicit class StreamedCassandraTable[CT <: CassandraTable[CT, T], T](
    val ct: CassandraTable[CT, T]
  ) extends AnyVal {

    /**
     * Gets a reactive streams [[org.reactivestreams.Subscriber]] with
     * batching capabilities for some phantom [[CassandraTable]]. This
     * subscriber is able to work for both finite short-lived streams
     * and never-ending long-lived streams. For the latter, a flushInterval
     * parameter can be used.
     *
     * @param batchSize the number of elements to include in the Cassandra batch
     * @param concurrentRequests the number of concurrent batch operations
     * @param batchType the type of the batch.
     *                  @see See [[http://docs.datastax.com/en/cql/3.1/cql/cql_reference/batch_r.html]] for further
     *                       explanation.
     * @param flushInterval used to schedule periodic batch execution even though the number of statements hasn't
     *                      been reached yet. Useful in never-ending streams that will never been completed.
     * @param completionFn  a function that will be invoked when the stream is completed
     * @param errorFn       a function that will be invoked when an error occurs
     * @param builder       an implicitly resolved [[RequestBuilder]] that wraps a phantom [[ExecutableStatement]].
     *                      Every T element that gets into the stream from the upstream is turned into a ExecutableStatement
     *                      by means of this builder.
     * @param system        the underlying [[ActorSystem]]. This [[org.reactivestreams.Subscriber]] implementation uses Akka
     *                      actors, but is not restricted to be used in the context of Akka Streams.
     * @param session       the Cassandra [[com.datastax.driver.core.Session]]
     * @param space         the Cassandra [[KeySpace]]
     * @param ev an evidence to get the T type removed by erasure
     * @return the [[org.reactivestreams.Subscriber]] to be connected to a reactive stream typically initiated by
     *         a [[org.reactivestreams.Publisher]]
     */
    def subscriber(
      batchSize: Int = DEFAULT_BATCH_SIZE,
      concurrentRequests: Int = DEFAULT_CONCURRENT_REQUESTS,
      batchType: BatchType = BatchType.Unlogged,
      flushInterval: Option[FiniteDuration] = None,
      completionFn: () => Unit = () => (),
      errorFn: Throwable => Unit = _ => ()
    )(implicit builder: RequestBuilder[CT, T],
      system: ActorSystem,
      session: Session,
      space: KeySpace,
      ev: Manifest[T]
    ): BatchSubscriber[CT, T] = {
      new BatchSubscriber[CT, T](
        ct.asInstanceOf[CT],
        builder,
        batchSize,
        concurrentRequests,
        batchType,
        flushInterval,
        completionFn,
        errorFn
      )
    }

    /**
      * Creates a stream publisher based on the default ReactiveStreams implementation.
      * This will use the underlying Play enumerator model to convert.
      *
      * @param session The Cassandra session to execute the enumeration within.
      * @param keySpace The target keyspace.
      * @return A publisher of records, publishing one record at a time.
      */
    def publisher()(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): Publisher[T] = {
      enumeratorToPublisher(ct.select.all().fetchEnumerator())
    }
  }

  implicit class PublisherConverter[T](val enumerator: PlayEnumerator[T]) extends AnyVal {
    def publisher: Publisher[T] = enumeratorToPublisher(enumerator)
  }


  /**
    * Returns the product of the arguments,
    * throwing an exception if the result overflows a {@code long}.
    *
    * @param x the first value
    * @param y the second value
    * @return the result
    * @throws ArithmeticException if the result overflows a long
    * @since 1.8
    */
  def multiplyExact(x: Long, y: Long): Long = {
    val r: Long = x * y
    val ax: Long = Math.abs(x)
    val ay: Long = Math.abs(y)
    if (((ax | ay) >> 31) != 0) {
      if (((y != 0) && (r / y != x)) || (x == Long.MinValue && y == -1)) {
        return Long.MaxValue
      }
    }
    r
  }

  final val Iteratee = PhantomIteratee

  implicit class ExecutableQueryStreamsAugmenter[
    T <: CassandraTable[T, R],
    R,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val query: SelectQuery[T, R, Limit, Order, Status, Chain, PS]) extends AnyVal {

    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return A play enumerator containing the results of the query.
      */
    def fetchEnumerator()(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        query.future() map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(query.fromRow)
        }
      }
    }

    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      * @param mod A modifier to apply to a statement.
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return A play enumerator containing the results of the query.
      */
    def fetchEnumerator(mod: Statement => Statement)(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        query.future(mod) map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(query.fromRow)
        }
      }
    }

    /**
      * Creates a Reactive Streams publisher from a root select block.
      * This will create a reactive streams publisher containing all the records found by this [[SelectQuery]].
      * @param session The database session in which to execute this.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher()(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = enumeratorToPublisher(query.fetchEnumerator())

    /**
      * Creates a Reactive Streams publisher from a root select block but also
      * allows passing through a query modifier.
      * This will create a reactive streams publisher containing all the records found by this [[SelectQuery]].
      * @param session The database session in which to execute this.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher(modifier: Statement => Statement)(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = {
      enumeratorToPublisher(query.fetchEnumerator(modifier))
    }
  }


  implicit class RootSelectBlockEnumerator[
    T <: CassandraTable[T, _],
    R
  ](val block: RootSelectBlock[T, R]) extends AnyVal {
    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator()(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        block.all().future() map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(block.rowFunc)
        }
      }
    }

    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        block.all().future(modifier) map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(block.rowFunc)
        }
      }
    }

    /**
      * Creates a Reactive Streams publisher from a root select block.
      * Because this is a [[RootSelectBlock]], the default execution profile
      * of this method will be to select all records in a table and stream them.
      * @param session The database session in which to execute this.
      * @param keySpace The keyspace in which to execute the query.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher()(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = enumeratorToPublisher(block.fetchEnumerator())

    /**
      * Creates a Reactive Streams publisher from a root select block but also
      * allows passing through a query modifier.
      * Because this is a [[RootSelectBlock]], the default execution profile
      * of this method will be to select all records in a table and stream them.
      * @param session The database session in which to execute this.
      * @param keySpace The keyspace in which to execute the query.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = {
      enumeratorToPublisher(block.fetchEnumerator(modifier))
    }
  }


  implicit class PreparedSelectQueryStream[
    T <: CassandraTable[T, _],
    R,
    Limit <: LimitBound
  ](val block: ExecutablePreparedSelectQuery[T, R, Limit]) extends AnyVal {
    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator()(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        block.future() map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(block.fromRow)
        }
      }
    }

    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): PlayEnumerator[R] = {
      PlayEnumerator.flatten {
        block.future(modifier) map { res =>
          Enumerator.enumerator(res) through Enumeratee.map(block.fromRow)
        }
      }
    }

    /**
      * Creates a Reactive Streams publisher from a root select block.
      * Because this is a [[RootSelectBlock]], the default execution profile
      * of this method will be to select all records in a table and stream them.
      * @param session The database session in which to execute this.
      * @param keySpace The keyspace in which to execute the query.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher()(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = enumeratorToPublisher(block.fetchEnumerator())

    /**
      * Creates a Reactive Streams publisher from a root select block but also
      * allows passing through a query modifier.
      * Because this is a [[RootSelectBlock]], the default execution profile
      * of this method will be to select all records in a table and stream them.
      * @param session The database session in which to execute this.
      * @param keySpace The keyspace in which to execute the query.
      * @param ctx The execution context to use.
      * @return A streams publisher interface clients can subscribe to.
      */
    def publisher(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContextExecutor
    ): Publisher[R] = {
      enumeratorToPublisher(block.fetchEnumerator(modifier))
    }
  }

}
