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
package com.websudos.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class SelectOptionalTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    OptionalPrimitives.insertSchema()
  }

  "Selecting the whole row" should "work fine when optional value defined" in {
    checkRow(gen[OptionalPrimitive])
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
        } yield (a, b)
      }
    }

    rcp successful {
      r => {
        r._1 contains row shouldEqual true

        r._2.isDefined shouldEqual true
        r._2.get shouldEqual row
      }
    }
  }
}
