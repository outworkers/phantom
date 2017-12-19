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
package com.outworkers.phantom.builder.query.db.specialized

import com.datastax.driver.core.exceptions.InvalidQueryException
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class SecondaryIndexTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.secondaryIndexTable.create.ifNotExists().future.block(defaultScalaTimeout)
  }

  it should "allow fetching a record by its secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      select <- database.secondaryIndexTable.select.where(_.id eqs sample.primary).one
      select2 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).allowFiltering().one()
    } yield (select, select2)

    whenReady(chain) { case (primary, secondary) =>
      info("Querying by primary key should return the record")
      primary.value shouldEqual sample

      info("Querying by the secondary index key should also return the record")
      secondary.value shouldEqual sample
    }
  }

  it should "allow fetching a record by its secondary index using prepared statements" in {
    val sample = gen[SecondaryIndexRecord]

    val query = database.secondaryIndexTable.select.where(_.secondary eqs ?).allowFiltering()

    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      select <- database.secondaryIndexTable.select.where(_.id eqs sample.primary).one
      bindable <- query.prepareAsync()
      select2 <- bindable.bind(sample.secondary).one()
    } yield (select, select2)

    whenReady(chain) { case (primary, secondary) =>
      info("Querying by primary key should return the record")
      primary.value shouldEqual sample

      info("Querying by the secondary index key should also return the record")
      secondary.value shouldEqual sample
    }
  }

  it should "allow updating the value of a secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val updated = gen[UUID]

    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      selected <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).allowFiltering().one()
      _ <- database.secondaryIndexTable.update.where(_.id eqs sample.primary).modify(_.secondary setTo updated).future()
      updated <- database.secondaryIndexTable.select.where(_.secondary eqs updated).allowFiltering().one()
    } yield (selected, updated)

    whenReady(chain) { case (primary, secondary) =>
      info("Querying by primary key should return the record")
      primary.value shouldEqual sample

      info("Querying by the secondary index key should also return the record")
      secondary.value shouldEqual sample.copy(secondary = updated)
    }
  }

  it should "not throw an error if filtering is not enabled when querying by secondary keys" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      select2 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield select2

    whenReady(chain) {
      res => res.value shouldEqual sample
    }
  }

  it should "throw an error when updating a record by its secondary key" in {
    val sample = gen[SecondaryIndexRecord]
    val updatedName = gen[String]
    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      select2 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      _ <- database.secondaryIndexTable.update.where(_.secondary eqs sample.secondary).modify(_.name setTo updatedName).future()
      select3 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    whenReady(chain.failed) { r =>
      r shouldBe an [InvalidQueryException]
    }
  }

  it should "throw an error when deleting a record by its secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      _ <- database.secondaryIndexTable.store(sample).future()
      select2 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      _ <- database.secondaryIndexTable.delete.where(_.secondary eqs sample.secondary).future()
      select3 <- database.secondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    whenReady(chain.failed) { r =>
      r shouldBe an [InvalidQueryException]
    }
  }

}
