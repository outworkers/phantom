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

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.websudos.phantom.tables.{ JodaRow, PrimitivesJoda }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class JodaDateTimeColumn extends BaseTest {
  val keySpace = "joda_columns_test"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      PrimitivesJoda.insertSchema()
    }
  }

  it should "correctly insert and extract a JodaTime date" in {
    val row = JodaRow.sample
    PrimitivesJoda.insertSchema()
    val w =
        PrimitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.int)
          .value(_.timestamp, row.bi)
          .future() flatMap  {
            _ => PrimitivesJoda.select.one
          }

    w successful {
      res => res.get shouldEqual row
    }
  }
}
