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
import com.outworkers.util.testing._
import shapeless._
import shapeless.ops.hlist.Take
import shapeless.o

class PartialSelectTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }



  def takeN[Source, N <: Nat, HL <: HList, Output](instance: Source, n: N)(
    implicit gen: Generic.Aux[Source, HL],
    taker: Take.Aux[HL, N, Output],
    ev: Mod.Aux
  ): Output = (gen to instance).take(n)

  "Partially selecting 2 fields" should "correctly select the fields" in {
    val row = gen[Primitive]

    val chain = for {
      _ <- TestDatabase.primitives.truncate.future()
      _ <- TestDatabase.primitives.store(row).future()
      listSelect <- TestDatabase.primitives.select(_.pkey).fetch
      oneSelect <- TestDatabase.primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
    } yield (listSelect, oneSelect)

    chain successful {
      case (res, res2) => {
        res shouldEqual List(row.pkey)
        res2.value shouldEqual Tuple2(row.long, row.boolean)
      }
    }

    "Partial selects" should "select 2 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future
        get <- TestDatabase.primitives.select(_.pkey, _.long).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        res => res.value shouldEqual expected
      }
    }

    "Partial selects" should "select 3 columns" in {

      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldEqual expected
      }
    }

    "Partial selects" should "select 4 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean, row.bDecimal)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }

    "Partial selects" should "select 5 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }

    "Partial selects" should "select 6 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }

    "Partial selects" should "select 7 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet).where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }

    "Partial selects" should "select 8 columns" in {
      val row = gen[Primitive]
      val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)

      val chain = for {
        _ <- TestDatabase.primitives.store(row).future()
        get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
          .where(_.pkey eqs row.pkey).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }

    "Partial selects" should "select 9 columns" in {
      val row = gen[WideRow]

      val expected = takeN(row, Nat._9)

      val chain = for {
        _ <- TestDatabase.wideTable.store(row).future()
        get <- TestDatabase.wideTable
          .select(_.id, _.field, _.field1, _.field2, _.field3, _.field4, _.field5, _.field6, _.field7)
          .where(_.id eqs row.id).one()
      } yield get

      whenReady(chain) {
        r => r.value shouldBe expected
      }
    }
  }
}
