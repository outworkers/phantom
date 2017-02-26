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
import com.outworkers.phantom.tables.{Recipe, TimeUUIDRecord}
import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._

class SelectFunctionsTesting extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.insertSchema()
    database.timeuuidTable.insertSchema()
  }

  it should "retrieve the writetime of a field from Cassandra" in {
    val record = gen[Recipe]


    val chain = for {
      _ <- database.recipes.store(record).future()
      timestamp <- database.recipes.select.function(t => writetime(t.description))
        .where(_.url eqs record.url).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
      shouldNotThrow {
        new DateTime(res.value / 1000)
      }
    }
  }

  it should "retrieve the dateOf of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => dateOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the unixTimestamp of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => unixTimestampOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
    }
  }

  it should "retrieve the TTL of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord].copy(id = UUIDs.timeBased())
    val timeToLive = 20

    val potentialList = List(timeToLive - 2, timeToLive - 1, timeToLive)

    val chain = for {
      _ <- database.timeuuidTable.store(record).ttl(timeToLive).future()
      timestamp <- database.timeuuidTable.select.function(t => ttl(t.name))
        .where(_.user eqs record.user)
        .and(_.id eqs record.id)
        .one()
    } yield timestamp

    whenReady(chain) { res =>
      res shouldBe defined
      potentialList should contain (res.value.value)
    }
  }
}
