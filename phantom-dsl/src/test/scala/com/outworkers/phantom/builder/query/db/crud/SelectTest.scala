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
import net.liftweb.http.js.JsObj
import net.liftweb.json.JsonParser

class SelectTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "Selecting the whole row" should "work fine" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      b <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield b

    chain successful {
      res => res.value shouldEqual row
    }
  }

  "Partial selects" should "select 2 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future
      get <- TestDatabase.primitives.select(_.pkey, _.long).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      res => res.value shouldEqual expected
    }
  }

  "Partial selects" should "select 3 columns" in {

    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldEqual expected
    }
  }

  "Partial selects" should "select 4 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 5 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 6 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 7 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet).where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 8 columns" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
        .where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      r => r.value shouldBe expected
    }
  }
}
