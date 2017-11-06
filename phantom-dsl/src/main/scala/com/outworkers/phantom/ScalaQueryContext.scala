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

import com.outworkers.phantom.builder.query.execution.{FutureMonad, PromiseInterface}
import com.outworkers.phantom.ops.QueryContext

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}

object ScalaFutureImplicits {

  implicit val monadInstance: FutureMonad[Future] = new FutureMonad[Future] {

    override def flatMap[A, B](source: Future[A])(fn: (A) => Future[B])(
      implicit ctx: ExecutionContextExecutor
    ): Future[B] = source flatMap fn

    override def map[A, B](source: Future[A])(f: (A) => B)(
      implicit ctx: ExecutionContextExecutor
    ): Future[B] = source map f

    override def pure[A](source: A): Future[A] = Future.successful(source)
  }
}

object ScalaPromiseInterface extends PromiseInterface[Promise, Future] {
  override def empty[T]: Promise[T] = Promise.apply[T]

  override def become[T](source: Promise[T], value: Future[T]): Promise[T] = source tryCompleteWith value

  override def future[T](source: Promise[T]): Future[T] = source.future

  override def failed[T](exception: Throwable): Future[T] = Future.failed[T](exception)

  override def apply[T](value: T): Future[T] = Future.successful(value)
}

class ScalaQueryContext extends QueryContext[Promise, Future, Duration](10.seconds)(
  ScalaFutureImplicits.monadInstance,
  ScalaPromiseInterface
) {

  override def blockAwait[T](f: Future[T], timeout: Duration): T = Await.result(f, timeout)

}
