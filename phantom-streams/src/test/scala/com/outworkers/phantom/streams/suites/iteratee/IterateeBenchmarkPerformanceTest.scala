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
import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone}
import org.scalameter.api.{Gen => MeterGen, gen => _, _}
import org.scalatest.time.SpanSugar._

import scala.concurrent.{Await, Future}

class IterateeBenchmarkPerformanceTest extends Bench.LocalTime with TestDatabase.connector.Connector {

  TestDatabase.primitivesJoda.createSchema()

  implicit object JodaTimeSampler extends Sample[DateTime] {
    override def sample: DateTime = DateTime.now(DateTimeZone.UTC)
  }

  val limit = 50
  val sampleGenLimit = 30

  val fs = for {
    step <- 1 to 3
    rows = Iterator.fill(limit)(gen[JodaRow])

    batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(TestDatabase.primitivesJoda.store(row))
    })
    w = batch.future()
    f = w map (_ => println(s"step $step was completed successfully"))
    r = Await.result(f, 20 seconds)
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
