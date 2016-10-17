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

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.reactivestreams._
import com.outworkers.phantom.tables.{JodaRow, TestDatabase}
import com.outworkers.util.testing._
import org.scalameter.api.{Gen => MeterGen, gen => _, _}
import org.scalatest.time.SpanSugar._

import scala.concurrent.{Await, Future}

class IterateeBenchmarkPerformanceTest extends PerformanceTest.Quickbenchmark with TestDatabase.connector.Connector {

  TestDatabase.primitivesJoda.insertSchema()

  val limit = 10000
  val sampleGenLimit = 30000

  val fs = for {
    step <- 1 to 3
    rows = Iterator.fill(limit)(gen[JodaRow])

    batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      val statement = TestDatabase.primitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.intColumn)
        .value(_.timestamp, row.timestamp)
      b.add(statement)
    })
    w = batch.future()
    f = w map (_ => println(s"step $step was completed successfully"))
    r = Await.result(f, 200 seconds)
  } yield f map (_ => r)

  Await.ready(Future.sequence(fs), 20 seconds)

  val sizes: MeterGen[Int] = MeterGen.range("size")(limit, sampleGenLimit, limit)

  performance of "Enumerator" in {
    measure method "enumerator" in {
      using(sizes) in {
        size => Await.ready(TestDatabase.primitivesJoda.select.limit(size).fetchEnumerator run Iteratee.forEach { r => }, 10 seconds)
      }
    }
  }
}
