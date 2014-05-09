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
package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ OptionalPrimitive, OptionalPrimitives }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class SelectOptionalTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace: String = "selectOptionalTest"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      OptionalPrimitives.insertSchema()
    }
  }

  "Selecting the whole row" should "work fine when optional value defined" in {
    checkRow(OptionalPrimitive.sample)
  }

  it should "work fine when optional value is empty" in {
    checkRow(OptionalPrimitive.none)
  }

  private def checkRow(row: OptionalPrimitive) {
    val rcp =  OptionalPrimitives.insert
      .value(_.pkey, row.pkey)
      .value(_.string, row.string)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        for {
          a <- OptionalPrimitives.select.fetch
          b <- OptionalPrimitives.select.where(_.pkey eqs row.pkey).one
        } yield (a contains row, b.get === row)
      }
    }

    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
      }
    }
  }
}
