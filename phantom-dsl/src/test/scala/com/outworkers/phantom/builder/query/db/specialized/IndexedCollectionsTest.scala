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
import com.outworkers.phantom.tables.TestRow
import com.outworkers.util.samplers._

class IndexedCollectionsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    if (cassandraVersion.value >= Version.`2.3.0`) {
      database.indexedCollectionsTable.createSchema()
    }

    if (cassandraVersion.value >= Version.`2.3.0`) {
      database.indexedEntriesTable.createSchema()
    }
  }

  it should "store a record and retrieve it with a CONTAINS query on the SET" in {
    val record = gen[TestRow]

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      one <- database.indexedCollectionsTable.select
        .where(_.setText contains record.setText.headOption.value)
        .fetch()
    } yield one

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

  it should "store a record and retrieve it with a prepared CONTAINS query on the SET" in {
    val record = gen[TestRow]

    val query = database.indexedCollectionsTable.select
      .where(_.setText contains ?)
      .prepareAsync()

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      prep <- query
      list <- prep.bind(record.setText.headOption.value).fetch()
    } yield list

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }

  }

  it should "store a record and retrieve it with a CONTAINS query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      one <- database.indexedCollectionsTable.select
        .where(_.mapTextToText contains record.mapTextToText.values.headOption.value)
        .fetch()
    } yield one

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

  it should "store a record and retrieve it with a CONTAINS query on the MAP using prepared statements" in {
    val record = gen[TestRow]

    val query = database.indexedCollectionsTable.select
      .where(_.mapTextToText contains ?)
      .prepareAsync()

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      results <- query.flatMap(_.bind(record.mapTextToText.values.headOption.value).fetch())
    } yield results

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      chain.failed.futureValue shouldBe an [InvalidQueryException]
    }
  }

  it should "store a record and retrieve it with a CONTAINS KEY query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      get <- database.indexedCollectionsTable
        .select
        .where(_.mapIntToText containsKey record.mapIntToText.keys.headOption.value)
        .fetch()
    } yield get

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

  it should "store a record and retrieve it with a CONTAINS KEY query on the MAP and prepared statements" in {
    val record = gen[TestRow]

    val chain = for {
      store <- database.indexedCollectionsTable.store(record).future()
      get <- database.indexedCollectionsTable
        .select
        .where(_.mapIntToText containsKey ?)
        .prepareAsync()
      select <- get.bind(record.mapIntToText.keys.headOption.value).fetch()
    } yield select

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

  it should "store a record and retrieve it with a CONTAINS ENTRY equals query on the map" in {
    val record = gen[TestRow].copy(mapIntToInt = Map(5 -> 10, 10 -> 15, 20 -> 25))

    val chain = for {
      store <- database.indexedEntriesTable.store(record).future()
      result <- database.indexedEntriesTable.select.where(_.mapIntToInt(20) eqs 25).fetch()
    } yield result

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

  it should "store a record and retrieve it with a prepared CONTAINS ENTRY equals query on the map" in {
    val record = gen[TestRow].copy(mapIntToInt = Map(5 -> 10, 10 -> 15, 20 -> 25))

    val query = database.indexedEntriesTable.select.where(_.mapIntToInt(20) eqs ?).prepareAsync()

    val chain = for {
      store <- database.indexedEntriesTable.store(record).future()
      result <- query.flatMap(_.bind(25).fetch())
    } yield result

    if (cassandraVersion.value > Version.`2.3.0`) {
      whenReady(chain) { res =>
        res.nonEmpty shouldEqual true
        res should contain (record)
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [InvalidQueryException]
      }
    }
  }

}
