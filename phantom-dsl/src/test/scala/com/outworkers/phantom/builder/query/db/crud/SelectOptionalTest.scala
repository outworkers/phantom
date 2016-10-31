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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class SelectOptionalTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.optionalPrimitives.insertSchema()
    if(session.v4orNewer) {
      TestDatabase.optionalPrimitivesCassandra22.insertSchema()
    }
  }

  "Selecting the whole row" should "work fine when optional value defined" in {
    checkRow(gen[OptionalPrimitive])
  }

  it should "work fine when optional value is empty" in {
    checkRow(OptionalPrimitive.empty)
  }


  if (session.v4orNewer) {
    "Selecting the whole row" should "work fine when cassandra 2.2 optional value defined" in {
      checkRow(gen[OptionalPrimitiveCassandra22])
    }

    it should "work fine when optional cassandra 2.2 value is empty" in {
      checkRow(OptionalPrimitiveCassandra22.none)
    }
  }

  private[this] def checkRow(row: OptionalPrimitive): Unit = {
    val rcp = for {
      store <- TestDatabase.optionalPrimitives.store(row).future()
      b <- TestDatabase.optionalPrimitives.select.where(_.pkey eqs row.pkey).one
    } yield b

    rcp successful {
      r => {
        r shouldBe defined
        r.value.bDecimal shouldEqual row.bDecimal
        r.value.bi shouldEqual row.bi
        r.value.boolean shouldEqual row.boolean
        r.value.date shouldEqual row.date
        r.value.double shouldEqual row.double
        r.value.float shouldEqual row.float
        r.value.inet shouldEqual row.inet
        r.value.int shouldEqual row.int
        r.value.long shouldEqual row.long
        r.value.pkey shouldEqual row.pkey
        r.value.string shouldEqual row.string
        r.value.timeuuid shouldEqual row.timeuuid
        r.value.uuid shouldEqual row.uuid
      }
    }
  }

  private[this] def checkRow(row: OptionalPrimitiveCassandra22): Unit = {
    val rcp = for {
      store <- TestDatabase.optionalPrimitivesCassandra22.store(row).future()
      b <- TestDatabase.optionalPrimitivesCassandra22.select.where(_.pkey eqs row.pkey).one
    } yield b

    rcp successful {
      r => {
        r shouldBe defined
        r.value.short shouldEqual row.short
        r.value.byte shouldEqual row.byte
      }
    }
  }
}
