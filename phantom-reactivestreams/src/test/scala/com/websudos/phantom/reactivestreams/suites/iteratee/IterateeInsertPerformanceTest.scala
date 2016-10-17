/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom.reactivestreams.suites.iteratee

import java.util.concurrent.atomic.AtomicLong
import com.outworkers.phantom.reactivestreams._

import scala.concurrent.{ Await, Future }

import org.scalatest.Matchers
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{ TestDatabase, JodaRow }
import com.outworkers.util.testing._

class IterateeInsertPerformanceTest extends BigTest with Matchers {

  implicit val s: PatienceConfiguration.Timeout = timeout(12 minutes)
  private[this] final val iteratorLimit = 10000

  it should "retrieve the right amount of results" in {
    TestDatabase.primitivesJoda.insertSchema()
    val fs = for {
      step <- 1 to 100
      rows = Iterator.fill(iteratorLimit)(gen[JodaRow])

      batch = rows.foldLeft(Batch.unlogged)((b, row) => {
        val statement = TestDatabase.primitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.intColumn)
          .value(_.timestamp, row.timestamp)
        b.add(statement)
      })
      w = batch.future()
      f = w map (_ => info(s"step $step has succeed"))
      r = Await.result(f, 200 seconds)
    } yield f map (_ => r)


    val combinedFuture = Future.sequence(fs) map {
      r => TestDatabase.primitivesJoda.select.count.one()
    }

    val counter: AtomicLong = new AtomicLong(0)
    val result = combinedFuture flatMap {
       rs => {
         info(s"done, inserted: $rs rows - start parsing")
         TestDatabase.primitivesJoda.select.fetchEnumerator run Iteratee.forEach { r => counter.incrementAndGet() }
       }
    }

    (result flatMap (_ => combinedFuture)) successful {
      r => {
        info(s"done, reading: ${counter.addAndGet(0)}")
        counter.get() shouldEqual r
      }
    }
  }
}
