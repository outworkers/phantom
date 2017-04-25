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
package com.outworkers.phantom.builder.query.prepared

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{PrimitiveRecord, PrimitiveCassandra22, Recipe}
import com.outworkers.util.samplers._

class PreparedInsertQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("user.timezone", "Canada/Pacific") // perform these tests in non utc timezone
    database.recipes.insertSchema()
    database.primitives.insertSchema()
    if (session.v4orNewer) {
      database.primitivesCassandra22.insertSchema()
    }
  }

  it should "serialize an insert query" in {

    val sample = gen[Recipe]

    val query = database.recipes.insert
      .p_value(_.uid, ?)
      .p_value(_.url, ?)
      .p_value(_.servings, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.description, ?)
      .p_value(_.lastcheckedat, ?)
      .p_value(_.props, ?)
      .prepare()

    val exec = query.bind(
      sample.uid,
      sample.url,
      sample.servings,
      sample.ingredients,
      sample.description,
      sample.lastCheckedAt,
      sample.props
    ).future()

    val chain = for {
      store <- exec
      get <- database.recipes.select.where(_.url eqs sample.url).one()
    } yield get

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "serialize a primitives insert query" in {
    val sample = gen[PrimitiveRecord]

    val query = database.primitives.insert
      .p_value(_.pkey, ?)
      .p_value(_.long, ?)
      .p_value(_.boolean, ?)
      .p_value(_.bDecimal, ?)
      .p_value(_.double, ?)
      .p_value(_.float, ?)
      .p_value(_.inet, ?)
      .p_value(_.int, ?)
      .p_value(_.date, ?)
      .p_value(_.uuid, ?)
      .p_value(_.bi, ?)
      .prepare()

    val exec = query.bind(
      sample.pkey,
      sample.long,
      sample.boolean,
      sample.bDecimal,
      sample.double,
      sample.float,
      sample.inet,
      sample.int,
      sample.date,
      sample.uuid,
      sample.bi
    ).future()

    val chain = for {
      store <- exec
      get <- database.primitives.select.where(_.pkey eqs sample.pkey).one()
    } yield get

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  if (session.v4orNewer) {
    it should "serialize a cassandra 2.2 primitives insert query" in {
      val sample = gen[PrimitiveCassandra22]

      val query = database.primitivesCassandra22.insert
        .p_value(_.pkey, ?)
        .p_value(_.short, ?)
        .p_value(_.byte, ?)
        .p_value(_.date, ?)
        .prepare()

      val exec = query.bind(sample).future()

      val selectQuery = database.primitivesCassandra22.select
        .where(_.pkey eqs ?)
        .prepare()

      val chain = for {
        store <- exec
        res <- selectQuery.bind(sample.pkey).one()
      } yield res

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }

  it should "excute a prepared insert with a bound TTL variable in the using clause" in {
    val usedTtl = 10

    val sample = gen[Recipe]

    val query = database.recipes.insert
      .p_value(_.uid, ?)
      .p_value(_.url, ?)
      .p_value(_.servings, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.description, ?)
      .p_value(_.lastcheckedat, ?)
      .p_value(_.props, ?)
      .ttl(?)
      .prepare()

    val exec = query.bind(
      sample.uid,
      sample.url,
      sample.servings,
      sample.ingredients,
      sample.description,
      sample.lastCheckedAt,
      sample.props,
      usedTtl
    )

    val chain = for {
      store <- exec.future()
      res <- database.recipes.select.where(_.url eqs sample.url).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "be able to bind a custom Scala BigDecimal" in {
    val sample = gen[PrimitiveRecord]

    val query = database.primitives.insert
      .p_value(_.pkey, ?)
      .p_value(_.long, ?)
      .p_value(_.boolean, ?)
      .p_value(_.bDecimal, ?)
      .p_value(_.double, ?)
      .p_value(_.float, ?)
      .p_value(_.inet, ?)
      .p_value(_.int, ?)
      .p_value(_.date, ?)
      .p_value(_.uuid, ?)
      .p_value(_.bi, ?)
      .prepare()

    val chain = for {
      store <- query.bind(sample).future()
      res <- database.primitives.select.where(_.pkey eqs sample.pkey).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }
}
