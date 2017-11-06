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
package com.outworkers.phantom.finagle.execution

import com.outworkers.phantom.builder.query.execution.{FutureMonad, PromiseInterface}
import com.outworkers.phantom.ops.QueryContext
import com.twitter.conversions.time._
import com.twitter.util.{Await, Duration, Future, Promise}

import scala.concurrent.ExecutionContextExecutor

object TwitterFutureImplicits {

  val monadInstance: FutureMonad[Future] = new FutureMonad[Future] {
    override def flatMap[A, B](fa: Future[A])(f: (A) => Future[B])(
      implicit ctx: ExecutionContextExecutor
    ): Future[B] = fa flatMap f

    override def map[A, B](source: Future[A])(f: (A) => B)(
      implicit ctx: ExecutionContextExecutor
    ): Future[B] = source map f

    override def pure[A](source: A): Future[A] = Future.value(source)
  }

}

object TwitterPromiseInterface extends PromiseInterface[Promise, Future] {
  override def empty[T]: Promise[T] = Promise.apply[T]

  override def become[T](source: Promise[T], value: Future[T]): Promise[T] = {
    source become value
    source
  }

  override def future[T](source: Promise[T]): Future[T] = source

  override def failed[T](exception: Throwable): Future[T] = Future.exception[T](exception)

  override def apply[T](value: T): Future[T] = Future.value(value)
}

class TwitterQueryContext extends QueryContext[Promise, Future, Duration](10.seconds)(
  TwitterFutureImplicits.monadInstance,
  TwitterPromiseInterface
) {

  override def blockAwait[T](f: Future[T], timeout: Duration): T = Await.result(f, timeout)

}