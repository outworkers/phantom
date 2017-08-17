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
package com.outworkers.phantom.finagle.query.prepared

import java.util.concurrent.atomic.AtomicInteger

import com.outworkers.phantom.builder.query.execution.ExactlyOncePromise
import com.outworkers.phantom.finagle._
import com.twitter.util.Future
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

class ExactlyOncePromiseTests extends FlatSpec with Matchers with ScalaFutures with TwitterFutures {

  it should "only execute the logic inside an exactly once promise a single time" in {
    val atomic = new AtomicInteger(0)

    val promise = new ExactlyOncePromise(Future(atomic.incrementAndGet()))

    val chain = for {
      one <- promise.future
      two <- promise.future
      three <- promise.future
      four <- promise.future
    } yield four

    whenReady(chain) { res =>
      res shouldEqual 1
    }
  }

}
