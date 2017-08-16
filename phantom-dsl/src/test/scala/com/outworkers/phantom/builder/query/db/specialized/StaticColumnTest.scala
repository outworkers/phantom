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

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.macros.TableHelper
import com.outworkers.phantom.tables.{StaticCollectionRecord, StaticCollectionTable}
import com.outworkers.util.samplers._

class StaticColumnTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.staticTable.createSchema()
    database.staticCollectionTable.createSchema()
  }

  it should "use a static value for a static column" in {

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val id2 = UUIDs.timeBased()
    val chain = for {
      // The first record holds the static value.
      insert <- database.staticTable.insert.value(_.id, id).value(_.clusteringId, id).value(_.staticTest, static).future()
      insert2 <- database.staticTable.insert.value(_.id, id).value(_.clusteringId, id2).future()
      select <- database.staticTable.select.where(_.id eqs id).and(_.clusteringId eqs id2).one()
    } yield select

    whenReady(chain) { res =>
      res.value.static shouldEqual static
    }
  }

  it should "update values in all rows" in {

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val static2 = "this_is_updated_static"
    val id2 = UUIDs.timeBased()
    val chain = for {

      // The first insert holds the first static value.
      insert <- database.staticTable.insert.value(_.id, id).value(_.clusteringId, id).value(_.staticTest, static).future()

      // The second insert updates the static value
      insert2 <- database.staticTable.insert.value(_.id, id).value(_.clusteringId, id2).value(_.staticTest, static2).future()

      // We query for the first record inserted.
      select <- database.staticTable.select.where(_.id eqs id).and(_.clusteringId eqs id).one()
    } yield select

    whenReady(chain) { res =>
      // The first record should hold the updated value.
      res.value.static shouldEqual static2
    }
  }

  it should "append a value to a static list and share the update among records" in {
    val id = gen[UUID]

    val helper = TableHelper[StaticCollectionTable, StaticCollectionRecord]

    val sample = gen[StaticCollectionRecord].copy(id = id)
    val sample2 = gen[StaticCollectionRecord].copy(id = id, list = sample.list)

    val chain = for {
      store1 <- db.staticCollectionTable.store(sample).future()
      store2 <- db.staticCollectionTable.store(sample2).future()
      update <- db.staticCollectionTable.update.where(_.id eqs id)
        .modify(_.staticList append "test")
        .future()

      rec <- database.staticCollectionTable
        .select
        .where(_.id eqs id)
        .and(_.clustering eqs sample.clustering)
        .one()
    } yield rec

    whenReady(chain) { res =>
      res.value.list shouldEqual sample.list ::: List("test")
    }
  }

}
