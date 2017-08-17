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
import com.outworkers.phantom.builder.primitives.{DerivedField, DerivedTupleField}
import com.outworkers.phantom.dsl.{?, _}
import com.outworkers.phantom.tables.{DerivedRecord, PrimitiveCassandra22, PrimitiveRecord, Recipe}
import com.outworkers.util.samplers._

class PreparedInsertQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("user.timezone", "Canada/Pacific") // perform these tests in non utc timezone
    database.recipes.createSchema()
    database.derivedPrimitivesTable.createSchema()
    database.primitives.createSchema()
    if (session.v4orNewer) {
      database.primitivesCassandra22.createSchema()
    }
  }

  it should "execute a prepared insert query" in {
    val sample = gen[Recipe]

    val query = database.recipes.insert
      .p_value(_.url, ?)
      .p_value(_.description, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.servings, ?)
      .p_value(_.lastcheckedat, ?)
      .p_value(_.props, ?)
      .p_value(_.uid, ?)
      .prepare()

    val chain = for {
      store <- query.bind(sample).future()
      get <- database.recipes.select.where(_.url eqs sample.url).one()
    } yield get

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "execute an asynchronous prepared insert query" in {
    val sample = gen[Recipe]

    val chain = for {
      query <- database.recipes.insert
        .p_value(_.url, ?)
        .p_value(_.description, ?)
        .p_value(_.ingredients, ?)
        .p_value(_.servings, ?)
        .p_value(_.lastcheckedat, ?)
        .p_value(_.props, ?)
        .p_value(_.uid, ?)
        .prepareAsync()
      store <- query.bind(sample).future()
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

    val chain = for {
      store <- query.bind(sample).future()
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

      val selectQuery = database.primitivesCassandra22.select
        .where(_.pkey eqs ?)
        .prepare()

      val chain = for {
        store <- query.bind(sample).future()
        res <- selectQuery.bind(sample.pkey).one()
      } yield res

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }

  it should "execute a prepared insert with a bound TTL variable in the using clause" in {
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

  it should "be able to bind a derived primitive" in {
    val sample = DerivedRecord(
      gen[UUID],
      gen[ShortString].value,
      gen[DerivedField],
      gen[DerivedTupleField]
    )

    val query = database.derivedPrimitivesTable.insert
      .p_value(_.id, ?)
      .p_value(_.description, ?)
      .p_value(_.rec, ?)
      .p_value(_.complex, ?)
      .prepare()

    val chain = for {
      store <- query.bind(sample).future()
      res <- database.derivedPrimitivesTable.select.where(_.id eqs sample.id).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "be able to asynchronously bind a derived primitive" in {
    val sample = DerivedRecord(
      gen[UUID],
      gen[ShortString].value,
      gen[DerivedField],
      gen[DerivedTupleField]
    )

    val chain = for {
      query <- database.derivedPrimitivesTable.insert.p_value(_.id, ?)
        .p_value(_.description, ?)
        .p_value(_.rec, ?)
        .p_value(_.complex, ?)
        .prepareAsync()
      store <- query.bind(sample).future()
      res <- database.derivedPrimitivesTable.select.where(_.id eqs sample.id).one()
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

  it should "be able to asynchronously bind a custom Scala BigDecimal" in {
    val sample = gen[PrimitiveRecord]

    val chain = for {
      query <- database.primitives.insert
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
        .prepareAsync()
       store <- query.bind(sample).future()
      res <- database.primitives.select.where(_.pkey eqs sample.pkey).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }
}
