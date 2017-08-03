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
import com.outworkers.phantom.dsl.{?, _}
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class PreparedSelectQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("user.timezone", "Canada/Pacific") // perform these tests in non utc timezone

    new CassandraTableStoreMethods(database.recipes).createSchema()
    database.articlesByAuthor.createSchema()
    database.primitives.createSchema()
    if (session.v4orNewer) {
      database.primitivesCassandra22.createSchema()
    }
  }

  it should "serialise and execute a prepared select with the same clause as a normal one" in {
    val recipe = gen[Recipe]

    val query = database.recipes.select.where(_.url eqs ?).prepare()

    val operation = for {
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select, select2)

    whenReady(operation) { case (items, items2) =>
      items shouldBe defined
      items.value shouldEqual recipe

      items2 shouldBe defined
      items2.value shouldEqual recipe
    }
  }

  it should "serialise and execute an async prepared select with the same clause as a normal one" in {
    val recipe = gen[Recipe]

    val operation = for {
      query <- database.recipes.select.where(_.url eqs ?).prepareAsync()
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select, select2)

    whenReady(operation) { case (items, items2) =>
      items shouldBe defined
      items.value shouldEqual recipe

      items2 shouldBe defined
      items2.value shouldEqual recipe
    }
  }

  it should "allow setting a limit using a prepared statement" in {
    val recipe = gen[Recipe]
    val limit = 1

    val query = database.recipes.select.where(_.url eqs ?).limit(?).prepare()

    val operation = for {
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url, limit).fetch()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select, select2)

    whenReady(operation) { case (items, items2) =>
      items.size shouldEqual limit
      items should contain (recipe)

      items2 shouldBe defined
      items2.value shouldEqual recipe
    }
  }

  it should "allow setting a limit using an async prepared statement" in {
    val recipe = gen[Recipe]
    val limit = 1

    val operation = for {
      query <- database.recipes.select.where(_.url eqs ?).limit(?).prepareAsync()
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url, limit).fetch()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select, select2)

    whenReady(operation) { case (items, items2) =>
      items.size shouldEqual limit
      items should contain (recipe)

      items2 shouldBe defined
      items2.value shouldEqual recipe
    }
  }

  it should "serialise and execute a prepared select statement with the correct number of arguments" in {
    val recipe = gen[Recipe]

    val query = database.recipes.select.where(_.url eqs ?).prepare()

    val operation = for {
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual recipe
    }
  }

  it should "serialise and execute am async prepared select statement with the correct number of arguments" in {
    val recipe = gen[Recipe]

    val operation = for {
      query <- database.recipes.select.where(_.url eqs ?).prepareAsync()
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual recipe
    }
  }

  it should "serialise and execute an async prepared statement with 2 arguments" in {
    val sample = gen[Article]
    val sample2 = gen[Article]
    val owner = gen[UUID]
    val category = gen[UUID]
    val category2 = gen[UUID]

    val op = for {
      query <- database.articlesByAuthor.select.where(_.author_id eqs ?).and(_.category eqs ?).prepareAsync()
      _ <- database.articlesByAuthor.store(owner, category, sample).future()
      _ <- database.articlesByAuthor.store(owner, category2, sample2).future()
      get <- query.bind(owner, category).one()
      get2 <- query.bind(owner, category2).one()
    } yield (get, get2)

    whenReady(op) { case (res, res2) =>
      res shouldBe defined
      res.value shouldEqual sample

      res2 shouldBe defined
      res2.value shouldEqual sample2
    }
  }

  it should "serialise and execute a primitives prepared select statement with the correct number of arguments" in {
    val primitive = gen[PrimitiveRecord]

    val query = database.primitives.select.where(_.pkey eqs ?).prepare()

    val operation = for {
      _ <- database.primitives.truncate.future
      _ <- database.primitives.store(primitive).future()
      select <- query.bind(primitive.pkey).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual primitive
    }
  }


  it should "serialise and execute an async primitives prepared select statement with the correct number of arguments" in {
    val primitive = gen[PrimitiveRecord]

    val operation = for {
      query <- database.primitives.select.where(_.pkey eqs ?).prepareAsync()
      _ <- database.primitives.truncate.future
      _ <- database.primitives.store(primitive).future()
      select <- query.bind(primitive.pkey).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual primitive
    }
  }

  if (session.v4orNewer) {
    it should "serialise and execute a primitives cassandra 2.2 prepared select statement with the correct number of arguments" in {
      val primitive = gen[PrimitiveCassandra22]

      val query = database.primitivesCassandra22.select.where(_.pkey eqs ?).prepare()

      val operation = for {
        _ <- database.primitivesCassandra22.truncate.future
        _ <- database.primitivesCassandra22.store(primitive).future()
        select <- query.bind(primitive.pkey).one()
      } yield select

      whenReady(operation) { items =>
        items shouldBe defined
        items.value shouldEqual primitive
      }
    }

    it should "serialise and execute an async primitives cassandra 2.2 prepared select statement with the correct number of arguments" in {
      val primitive = gen[PrimitiveCassandra22]

      val operation = for {
        query <- database.primitivesCassandra22.select.where(_.pkey eqs ?).prepareAsync()
        _ <- database.primitivesCassandra22.truncate.future
        _ <- database.primitivesCassandra22.store(primitive).future()
        select <- query.bind(primitive.pkey).one()
      } yield select

      whenReady(operation) { items =>
        items shouldBe defined
        items.value shouldEqual primitive
      }
    }
  }
}
