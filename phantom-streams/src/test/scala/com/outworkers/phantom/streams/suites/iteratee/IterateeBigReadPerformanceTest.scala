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
package com.outworkers.phantom.streams.suites.iteratee

import java.util.concurrent.atomic.AtomicLong

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams._
import com.outworkers.util.testing._
import org.scalatest.concurrent.ScalaFutures

class IterateeBigReadPerformanceTest extends BigTest with ScalaFutures {

  it should "read the correct number of records found in the table" in {
    val counter = new AtomicLong(0)

    val result = database.primitivesJoda.select.fetchEnumerator run Iteratee.forEach {
      r => counter.incrementAndGet()
    }

    result.successful {
      query => {
        val count = counter.getAndIncrement()
        info(s"Done, reading: $count elements from the table.")
        count shouldEqual 2000000
      }
    }
  }
}
