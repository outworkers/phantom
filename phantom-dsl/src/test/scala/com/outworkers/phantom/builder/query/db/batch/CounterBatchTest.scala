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
package com.outworkers.phantom.builder.query.db.batch

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase

class CounterBatchTest extends PhantomSuite {

  val x = TestDatabase.counterTableTest
  val y = TestDatabase.secondaryCounterTable

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.counterTableTest.createSchema()
    TestDatabase.secondaryCounterTable.createSchema()
  }

  it should "create a batch query to perform several updates in a single table" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))

    val chain = for {
      batched <- ft.future()
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value shouldEqual 2500
      }
    }
  }

  it should "create a batch query to update counters in several tables" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .future()

    val chain = for {
      batched <- ft
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- TestDatabase.secondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, updated) => {
        info("The first counter select should return the record")
        initial shouldBe defined
        info("and the counter value should match the sum of the +=s")
        initial.value shouldEqual 2500

        info("The second counter select should return the record")
        updated shouldBe defined
        info("and the counter value should match the sum of the +=s")
        updated.value shouldEqual 2500
      }
    }
  }

  it should "create a batch query to counters in several tables while alternating between += and -=" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))

      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .future()

    val chain = for {
      batched <- ft
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- TestDatabase.secondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, updated) => {
        info("The first counter select should return the record")
        initial shouldBe defined
        info("and the counter value should match the sum of the +=s")
        initial.value shouldEqual 500

        info("The second counter select should return the record")
        updated shouldBe defined
        info("and the counter value should match the sum of the +=s")
        updated.value shouldEqual 500
      }
    }
  }
}
