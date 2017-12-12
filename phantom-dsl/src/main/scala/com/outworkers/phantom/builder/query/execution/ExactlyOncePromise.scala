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
package com.outworkers.phantom.builder.query.execution

import java.util.concurrent.atomic.AtomicBoolean

import com.datastax.driver.core.{Session, Statement}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.outworkers.phantom.{Manager, ResultSet}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits

import scala.concurrent.ExecutionContextExecutor

trait PromiseInterface[P[_], F[_]] {

  def empty[T]: P[T]

  def apply[T](value: T): F[T]

  def become[T](source: P[T], value: F[T]): P[T]

  def future[T](source: P[T]): F[T]

  def failed[T](exception: Throwable): F[T]

  def adapter(implicit monad: FutureMonad[F]): GuavaAdapter[F] = new GuavaAdapter[F] with SessionAugmenterImplicits {

    override def fromGuava[T](source: ListenableFuture[T])(
      implicit executor: ExecutionContextExecutor
    ): F[T] = {
      val promise = empty[T]

      val callback = new FutureCallback[T] {
        def onSuccess(result: T): Unit = {
          val _ = become(promise, apply(result))
        }

        def onFailure(err: Throwable): Unit = {
          val _ = become(promise, failed(err))
        }
      }

      Futures.addCallback(source, callback, executor)
      future(promise)
    }

    override def fromGuava(in: Statement)(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): F[ResultSet] = {
      Manager.logger.info("Executing query: {}", in)
      fromGuava(session.executeAsync(in)).map(res => ResultSet(res, session.protocolVersion))
    }
  }
}

class ExactlyOncePromise[P[_], F[_], T](
  fn: => F[T]
)(
  implicit fMonad: FutureMonad[F],
  interface: PromiseInterface[P, F]
) {

  private[this] val promise: P[T] = interface.empty[T]

  def future: F[T] = interface.future(init)

  private[this] val flag = new AtomicBoolean(false)

  private[this] def init: P[T] = {
    if (flag.compareAndSet(false, true)) {
      interface.become(promise, fn)
    }
    promise
  }
}
