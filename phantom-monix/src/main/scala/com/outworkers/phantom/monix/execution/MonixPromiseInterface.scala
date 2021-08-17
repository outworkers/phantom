/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.monix.execution

import com.datastax.driver.core.{Session, Statement}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.execution.{FutureMonad, GuavaAdapter, PromiseInterface}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import monix.eval.Task
import monix.execution.Cancelable

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Promise}

class MonixPromiseInterface extends PromiseInterface[Task, Task]{
  override def empty[T]: Task[T] = Task.fromFuture(Promise.apply[T].future)

  override def apply[T](value: T): Task[T] = Task.now(value)

  override def become[T](source: Task[T], value: Task[T]): Task[T] = {
    Task.raiseError(new Exception("This method call has been hit: become"))
  }

  override def adapter(
    implicit monad: FutureMonad[Task]
  ): GuavaAdapter[Task] = new GuavaAdapter[Task] with SessionAugmenterImplicits {
    override def fromGuava[T](source: ListenableFuture[T])(
      implicit executor: ExecutionContext
    ): Task[T] = {
      Task.create[T] { (_, cb) =>
        val callback = new FutureCallback[T] {
          def onSuccess(result: T): Unit = {
            cb.onSuccess(result)
          }

          def onFailure(err: Throwable): Unit = {
            cb.onError(err)
          }
        }

        Futures.addCallback(source, callback, executor.asInstanceOf[ExecutionContextExecutor])
        Cancelable { () =>
          source.cancel(true)
        }
      }
    }

    override def fromGuava(in: Statement)(
      implicit session: Session,
      ctx: ExecutionContext
    ): Task[ResultSet] = {
      fromGuava(session.executeAsync(in)).map(res => ResultSet(res, session.protocolVersion))
    }
  }

  override def future[T](source: Task[T]): Task[T] = source

  override def failed[T](exception: Throwable): Task[T] = Task.raiseError(exception)
}
