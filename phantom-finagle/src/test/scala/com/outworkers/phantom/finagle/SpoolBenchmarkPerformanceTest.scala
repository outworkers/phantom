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
package com.outworkers.phantom.finagle

import com.outworkers.phantom.tables.{JodaRow, TestDatabase}
import com.outworkers.util.samplers._
import com.twitter.util.{ Await, Future }
import org.joda.time.{DateTime, DateTimeZone}
import org.scalameter.api.{Gen => MeterGen, gen => _, _}
import com.twitter.conversions.time._

class SpoolBenchmarkPerformanceTest extends Bench.LocalTime with TestDatabase.connector.Connector {

  TestDatabase.primitivesJoda.createSchema()
  TestDatabase.primitivesJoda.truncate()

  implicit object JodaTimeSampler extends Sample[DateTime] {
    override def sample: DateTime = DateTime.now(DateTimeZone.UTC)
  }

  val sampleSize = 30000
  Iterator.fill(sampleSize)(gen[JodaRow]).grouped(256).foreach { rs =>
    val chain = rs.map(r => TestDatabase.primitivesJoda.store(r).future.map(_ => ()))
    Await.ready(Future.collect(chain), 1.minutes)
  }

  val sizes: MeterGen[Int] = MeterGen.range("size")(10000, 30000, 10000)

  performance of "ResultSpool" in {
    measure method "fetchSpool" in {
      using(sizes) in { size =>
        Await.ready {
          TestDatabase.primitivesJoda.select.limit(size).fetchSpool().flatMap(_.force)
        }
      }
    }
  }
}
