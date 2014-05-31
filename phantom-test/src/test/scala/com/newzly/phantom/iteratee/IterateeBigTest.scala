/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.iteratee

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.{ Await, Future }
import org.scalatest.Matchers
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ PrimitivesJoda, JodaRow }
import com.newzly.util.testing.AsyncAssertionsHelper._


class IterateeBigTest extends BigTest with Matchers {
  val keySpace: String = "BigIterateeTestSpace"

  implicit val s: PatienceConfiguration.Timeout = timeout(12 minutes)

  it should "get result fine" in {
    PrimitivesJoda.insertSchema()
    val fs = for {
      step <- 1 to 100
      rows = Iterator.fill(10000)(JodaRow.sample)

      batch = rows.foldLeft(new BatchStatement())((b, row) => {
        val statement = PrimitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.int)
          .value(_.timestamp, row.bi)
        b.add(statement)
      })
      w = batch.future()
      f = w map (_ => println(s"step $step has succeed") )
      r = Await.result(f, 200 seconds)
    } yield f map (_ => r)


    val combinedFuture = Future.sequence(fs) map {
      r => PrimitivesJoda.count.one()
    }

    val counter: AtomicLong = new AtomicLong(0)
    val result = combinedFuture flatMap {
       rs => {
         info(s"done, inserted: $rs rows - start parsing")
         PrimitivesJoda.select.setFetchSize(10000).fetchEnumerator run Iteratee.forEach { r=> counter.incrementAndGet() }
       }
    }

    (result flatMap (_ => combinedFuture)) successful {
      r => {
        info(s"done, reading: ${counter.get}")
        counter.get() shouldEqual r
      }
    }
  }
}
