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

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams._
import com.outworkers.phantom.tables.{JodaRow, TestDatabase}
import com.outworkers.util.testing._
import org.scalameter.api.{Gen => MeterGen, gen => _, _}
import org.scalatest.time.SpanSugar._

import scala.concurrent.{Await, Future}

class IterateeBenchmarkPerformanceTest extends Bench.LocalTime with TestDatabase.connector.Connector {

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
