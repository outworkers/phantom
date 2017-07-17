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
package com.outworkers.phantom.builder.query.db.select

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._
import shapeless._
import syntax.std.tuple._

class PartialSelectTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.createSchema()
    database.wideTable.createSchema()
  }

  "Partially selecting 1 fields" should "select 1 field" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(row).future()
      oneSelect <- database.primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
    } yield oneSelect

    whenReady(chain) { res =>
      res.value shouldEqual row.long -> row.boolean
    }
  }

  "Partial selects" should "select 2 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long)

    val chain = for {
      _ <- database.primitives.store(row).future
      get <- database.primitives.select(_.pkey, _.long).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      res => res.value shouldEqual expected
    }
  }

  "Partial selects" should "select 3 columns" in {

    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldEqual expected
    }
  }

  "Partial selects" should "select 4 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 5 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 6 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 7 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet).where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 8 columns" in {
    val row = gen[PrimitiveRecord]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)

    val chain = for {
      _ <- database.primitives.store(row).future()
      get <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
        .where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 9 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._9)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 10 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._10)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 11 columns" in {
    val row = gen[WideRow]
    val expected = row.take(Nat._11)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 12 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._12)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 13 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._13)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 14 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._14)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 15 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._15)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 16 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._16)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 17 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._17)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14,
          _.field15
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 18 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._18)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14,
          _.field15,
          _.field16
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 19 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._19)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14,
          _.field15,
          _.field16,
          _.field17
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 20 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._20)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14,
          _.field15,
          _.field16,
          _.field17,
          _.field18
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }

  "Partial selects" should "select 21 columns" in {
    val row = gen[WideRow]

    val expected = row.take(Nat._21)

    val chain = for {
      _ <- database.wideTable.store(row).future()
      get <- database.wideTable
        .select(
          _.id,
          _.field,
          _.field1,
          _.field2,
          _.field3,
          _.field4,
          _.field5,
          _.field6,
          _.field7,
          _.field8,
          _.field9,
          _.field10,
          _.field11,
          _.field12,
          _.field13,
          _.field14,
          _.field15,
          _.field16,
          _.field17,
          _.field18,
          _.field19
        )
        .where(_.id eqs row.id).one()
    } yield get

    whenReady(chain) {
      r => r.value shouldBe expected
    }
  }
}
