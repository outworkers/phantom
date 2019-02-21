/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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

import com.outworkers.phantom.ops.QueryContext
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration._

class MonixQueryContext()(implicit scheduler: Scheduler) extends QueryContext[Task, Task, Duration](10.seconds)(
  MonixImplicits.taskMonad,
  MonixImplicits.taskInterface
) {
  override def blockAwait[T](f: Task[T], timeout: Duration): T = {
    Await.result(f.runAsync, timeout)
  }
}
