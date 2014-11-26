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
import scala.concurrent.{ Await, Future }
import scala.concurrent
// import scala.concurrent.duration.DurationInt

import org.scalameter.api.{ Gen => MeterGen, gen => _, _ }
import org.scalatest.Matchers
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{ PrimitivesJoda, JodaRow }
import com.websudos.phantom.testing.TestZookeeperConnector
import com.websudos.util.testing._


class IterateeBenchmark extends PerformanceTest.Quickbenchmark
  with TestZookeeperConnector {

  PrimitivesJoda.insertSchema()
  val fs = for {
    step <- 1 to 3
    rows = Iterator.fill(10000)(gen[JodaRow])

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

  Await.ready(Future.sequence(fs), 20 seconds)

  val sizes: MeterGen[Int] = MeterGen.range("size")(10000, 30000, 10000)

  performance of "Enumerator" in {
    measure method "enumerator" in {
      using(sizes) in {
        size => Await.ready(PrimitivesJoda.select.limit(size).fetchEnumerator run Iteratee.forEach { r => }, 10 seconds)
      }
    }
  }
}
