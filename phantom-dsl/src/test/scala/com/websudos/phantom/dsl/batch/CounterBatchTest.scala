/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
