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
package com.websudos.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class JodaDateTimeColumn extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    PrimitivesJoda.insertSchema()
  }

  it should "correctly insert and extract a JodaTime date" in {
    val row = gen[JodaRow]

    val w = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)
      .future() flatMap  {
        _ => PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
      }

    w successful {
      res => res.get shouldEqual row
    }
  }

  it should "correctly insert and extract a JodaTime date with Twitter Futures" in {
    val row = gen[JodaRow]

    val w = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)
      .execute() flatMap  {
        _ => PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
      }

    w successful {
      res => res.get shouldEqual row
    }
  }
}
