/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.iteratee

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.concurrent.ScalaFutures
import com.websudos.phantom.tables.PrimitivesJoda
import com.websudos.util.testing._

class IterateeBigReadTest extends BigTest with ScalaFutures {

  it should "read the records found in the table" in {
    val counter: AtomicLong = new AtomicLong(0)
    val result = PrimitivesJoda.select.fetchEnumerator run Iteratee.forEach {
      r => counter.incrementAndGet()
    }

    result.successful {
      query => {
        info(s"done, reading: ${counter.get}")
        counter.get() shouldEqual 2000000
      }
    }
  }
}
