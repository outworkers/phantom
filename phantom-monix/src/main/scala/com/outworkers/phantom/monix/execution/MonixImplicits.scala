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

import com.outworkers.phantom.builder.query.execution.{FutureMonad, PromiseInterface}
import monix.eval.Task

import scala.concurrent.ExecutionContextExecutor

object MonixImplicits {

  implicit val taskInterface: PromiseInterface[Task, Task] = new MonixPromiseInterface

  implicit val taskMonad: FutureMonad[Task] = new FutureMonad[Task] {

    override def flatMap[A, B](source: Task[A])(fn: (A) => Task[B])(
      implicit ctx: ExecutionContextExecutor
    ): Task[B] = source flatMap fn

    override def map[A, B](source: Task[A])(f: (A) => B)(
      implicit ctx: ExecutionContextExecutor
    ): Task[B] = source map f

    override def pure[A](source: A): Task[A] = Task.pure(source)
  }

}
