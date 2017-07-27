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

import cats.Monad
import com.outworkers.phantom.ops.QueryContext

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import cats.instances.future._
import com.outworkers.phantom.builder.query.execution.PromiseInterface

object ScalaFutureImplicits {

  val monadInstance: Monad[Future] = catsStdInstancesForFuture(Manager.scalaExecutor)

}

object ScalaPromiseInterface extends PromiseInterface[Promise, Future] {
  override def empty[T]: Promise[T] = Promise.apply[T]

  override def become[T](source: Promise[T], value: Future[T]): Promise[T] = source tryCompleteWith value

  override def future[T](source: Promise[T]): Future[T] = source.future
}

class ScalaQueryContext extends QueryContext[Promise, Future, Duration](10.seconds)(
  ScalaFutureImplicits.monadInstance,
  ScalaPromiseInterface,
  ScalaGuavaAdapter
) {
  override def await[T](f: Future[T], timeout: Duration): T = Await.result(f, timeout)



}
