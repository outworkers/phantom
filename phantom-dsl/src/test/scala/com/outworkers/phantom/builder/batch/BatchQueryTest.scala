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
package com.outworkers.phantom.builder.batch

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.JodaRow
import com.outworkers.util.samplers._
import org.joda.time.DateTime

class BatchQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.primitivesJoda.createSchema()
  }

  it should "correctly execute a chain of INSERT queries" in {
    val rows = genList[JodaRow]()

    val batch = Batch.logged.add(rows.map(r => database.primitivesJoda.store(r)): _*)

    val chain = for {
      ex <- database.primitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual rows.size
    }
  }

  it should "correctly execute a chain of queries with a ConsistencyLevel set" in {
    val rows = genList[JodaRow]()

    val batch = Batch.logged.add(rows.map(r => database.primitivesJoda.store(r)): _*)

    val chain = for {
      ex <- database.primitivesJoda.truncate.future()
      batchDone <- batch.consistencyLevel_=(ConsistencyLevel.ONE).future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual rows.size
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts" in {
    val row = gen[JodaRow]

    val statement1 = database.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- database.primitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual 1
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = database.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- database.primitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual 1
    }
  }

  it should "correctly execute an UPDATE/DELETE pair batch query" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement1 = database.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val statement2 = database.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.intColumn)
      .value(_.timestamp, row3.timestamp)

    val statement3 = database.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo  row2.timestamp)

    val statement4 = database.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.future()
      s3 <- statement2.future()
      b <- batch.future()
      updated <- database.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
      deleted <- database.primitivesJoda.select.where(_.pkey eqs row3.pkey).one()
    } yield (updated, deleted)

    whenReady(w) { case (updated, deleted) =>
      updated.value shouldEqual row2
      deleted shouldNot be (defined)
    }
  }

  ignore should "prioritise batch updates in a last first order" in {
    val row = gen[JodaRow]

    val statement1 = database.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.logged
      .add(statement1)
      .add(
        database.primitivesJoda.update
          .where(_.pkey eqs row.pkey)
          .modify(_.intColumn setTo row.intColumn)
      ).add(
        database.primitivesJoda.update
          .where(_.pkey eqs row.pkey)
          .modify(_.intColumn setTo (row.intColumn + 10))
      ).add(
        database.primitivesJoda.update
          .where(_.pkey eqs row.pkey)
          .modify(_.intColumn setTo (row.intColumn + 15))
      ).add(
        database.primitivesJoda.update
          .where(_.pkey eqs row.pkey)
          .modify(_.intColumn setTo (row.intColumn + 20))
      )

    val chain = for {
      done <- batch.future()
      updated <- database.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    whenReady(chain) { res =>
      res.value.intColumn shouldEqual (row.intColumn + 20)
    }
  }

  ignore should "prioritise batch updates based on a timestamp" in {
    val row = gen[JodaRow]
    val last = gen[DateTime]
    val last1 = last.withDurationAdded(100, 5)
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = database.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.logged
      .add(statement1.timestamp(last.getMillis))
      .add(database.primitivesJoda.update.where(_.pkey eqs row.pkey)
        .modify(_.intColumn setTo (row.intColumn + 10))
        .timestamp(last1.getMillis))
      .add(database.primitivesJoda.update.where(_.pkey eqs row.pkey)
        .modify(_.intColumn setTo (row.intColumn + 15)))
        .timestamp(last2.getMillis)

    val chain = for {
      done <- batch.future()
      updated <- database.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    whenReady(chain) { res =>
      res shouldEqual defined
      res.value.intColumn shouldEqual (row.intColumn + 15)
    }
  }
}
