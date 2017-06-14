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

import java.util.concurrent.atomic.AtomicInteger

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class IterateePerformanceTest extends PhantomSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(2 minutes)

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.createSchema()
    database.primitivesJoda.createSchema()
  }

  it should "retrieve the correct number of results from the database and collect them using an iterator" in {
    val rows = for (i <- 1 to 100) yield gen[JodaRow]
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(db.primitivesJoda.store(row))
    })

    val chain = for {
      truncate <- db.primitivesJoda.truncate().future()
      w <- batch.future()
      seqR <- db.primitivesJoda.select.fetchEnumerator run Iteratee.collect()
    } yield seqR

    whenReady(chain) { seqR =>
      seqR should contain theSameElementsAs rows
      seqR.size shouldEqual rows.size
    }
  }

  it should "retrieve the right number of records using asynchronous iterators" in {
    val sampleSize = 50
    val rows = genList[PrimitiveRecord](sampleSize)
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(database.primitives.store(row))
    })

    val counter = new AtomicInteger(0)

    val chain = for {
      truncate <- db.primitives.truncate.future()
      execBatch <- batch.future()
      enum <- db.primitives.select.fetchEnumerator() run Iteratee.forEach(x => counter.incrementAndGet())
    } yield enum

    whenReady(chain) { _ =>
      counter.intValue() shouldEqual rows.size
    }
  }
}
