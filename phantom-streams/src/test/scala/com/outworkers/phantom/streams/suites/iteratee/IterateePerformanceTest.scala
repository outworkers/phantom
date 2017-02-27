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
    database.primitives.insertSchema()
    database.primitivesJoda.insertSchema()
  }

  it should "get retrieve the correct number of results from the database and collect them using an iterator" in {
    val rows = for (i <- 1 to 1000) yield gen[JodaRow]
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      val statement = TestDatabase.primitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.intColumn)
        .value(_.timestamp, row.timestamp)
      b.add(statement)
    })

    val w = batch.future() map (_ => TestDatabase.primitivesJoda.select.fetchEnumerator)
    whenReady(w) { en =>
      val result = en run Iteratee.collect()
      whenReady(result) { seqR =>
        for (row <- seqR) rows.contains(row) shouldEqual true
        seqR.size shouldEqual rows.size
      }
    }
  }

  it should "get correctly retrieve the right number of records using asynchronous iterators" in {
    val rows = for (i <- 1 to 100) yield gen[Primitive]
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(database.primitives.store(row))
    })

    val w = database.primitives.truncate.future().flatMap {
      _ => batch.future().map(_ => TestDatabase.primitives.select.fetchEnumerator())
    }

    val counter: AtomicInteger = new AtomicInteger(0)
    val m = w flatMap {
      _ run Iteratee.forEach(x => {
        counter.incrementAndGet(); assert(rows.contains(x))
      })
    }

    whenReady(m) { _ =>
      counter.intValue() shouldEqual rows.size
    }
  }
}
