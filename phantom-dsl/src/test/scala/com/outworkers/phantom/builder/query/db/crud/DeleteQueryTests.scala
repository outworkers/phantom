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
import com.outworkers.util.samplers._

class DeleteQueryTests extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.primitives.createSchema()
  }

  "A delete query" should "delete a row by its single primary key" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.store(row).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey).future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row
      r2 shouldBe empty
    }
  }

  "A delete query" should "delete a row by its single primary key if a single condition is met" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.store(row).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is row.int).future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row
      r2 shouldBe empty
    }
  }

  "A delete query" should "not delete a row by its single primary key if a single condition is not met" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.store(row).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is (row.int + 1)).future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row

      info("The row should not have been deleted as the condition was not met")
      r2 shouldBe defined
    }
  }

  it should "allow specifying a custom consistency level" in {
    val row = gen[PrimitiveRecord]
    val time = gen[DateTime]

    val chain = for {
      store <- database.primitives.store(row).timestamp(time).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey)
        .consistencyLevel_=(ConsistencyLevel.ONE)
        .future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row

      info("The row should not have been deleted as the condition was not met")
      r2 shouldBe empty
    }
  }

  "Using a timestamp" should "delete the record if the delete timestamp is the highest" in {
    val row = gen[PrimitiveRecord]
    val time = gen[DateTime]

    val chain = for {
      store <- database.primitives.store(row).timestamp(time).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey).timestamp(time.plusSeconds(1)).future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row

      info("The row should have been deleted as the delete timestamp was higher than the insert ")
      r2 shouldBe empty
    }
  }

  "Using a timestamp" should "not delete the record if the delete timestamp is the lowest" in {
    val row = gen[PrimitiveRecord]
    val time = gen[DateTime]

    val chain = for {
      store <- database.primitives.store(row).timestamp(time).future()
      inserted <- database.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- database.primitives.delete.where(_.pkey eqs row.pkey).timestamp(time.minusSeconds(1)).future()
      deleted <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    whenReady(chain) { case (r1, r2) =>
      r1.value shouldEqual row

      info("The row should not have been deleted as the delete timestamp was lower than the insert timestamp")
      r2.value shouldBe row
    }
  }

}
