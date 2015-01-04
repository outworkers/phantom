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
package com.websudos.phantom.dsl.batch

import com.datastax.driver.core.utils.UUIDs
import com.websudos.util.testing._
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.phantom.tables.{CounterTableTest, SecondaryCounterTable}

class CounterBatchTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    CounterTableTest.insertSchema()
    SecondaryCounterTable.insertSchema()
  }

  it should "create a batch query to perform several updates in a single table" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))

    val chain = for {
      batched <- ft.future()
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to perform several updates in a single table with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .execute()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to update counters in several tables" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .future()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 2500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to update counters in several tables with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .execute()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).get()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).get()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 2500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to counters in several tables while alternating between increment and decrement" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))

      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .future()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 500L
      }
    }
  }

  it should "create a batch query to counters in several tables while alternating between increment and decrement with Twitter futures" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))

      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries decrement 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .future()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 500L
      }
    }
  }
}
