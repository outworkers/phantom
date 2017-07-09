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
import com.outworkers.phantom.tables.{JodaRow, TestDatabase}
import com.outworkers.util.samplers._
import org.scalatest.Matchers
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import scala.concurrent.{Await, Future}

class IterateeInsertPerformanceTest extends BigTest with Matchers {

  implicit val s: PatienceConfiguration.Timeout = timeout(12 minutes)
  private[this] final val iteratorLimit = 100

  it should "retrieve the right amount of results" in {
    TestDatabase.primitivesJoda.createSchema()
    val fs = for {
      step <- 1 to 50
      rows = Iterator.fill(iteratorLimit)(gen[JodaRow])

      batch = rows.foldLeft(Batch.unlogged)((b, row) => {
        val statement = TestDatabase.primitivesJoda.store(row)
        b.add(statement)
      })
      w = batch.future()
      f = w map (_ => info(s"step $step has succeed"))
      r = Await.result(f, 200 seconds)
    } yield f map (_ => r)

    val counter: AtomicLong = new AtomicLong(0)

    val chain = for {
      res <- TestDatabase.primitivesJoda.select.fetchEnumerator run Iteratee.forEach { r => counter.incrementAndGet() }
      seq <- Future.sequence(fs)
      count <- TestDatabase.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { count =>
      info(s"done, reading: ${counter.addAndGet(0)}")
      Some(counter.get()) shouldEqual count
    }
  }
}
