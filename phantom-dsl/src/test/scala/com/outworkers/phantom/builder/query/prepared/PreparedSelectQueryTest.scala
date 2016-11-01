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
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class PreparedSelectQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("user.timezone", "Canada/Pacific") // perform these tests in non utc timezone
    TestDatabase.recipes.insertSchema()
    TestDatabase.articlesByAuthor.insertSchema()
    TestDatabase.primitives.insertSchema()
    if(session.v4orNewer) {
      TestDatabase.primitivesCassandra22.insertSchema()
    }
  }

  it should "serialise and execute a prepared select with the same clause as a normal one" in {
    val recipe = gen[Recipe]

    val query = TestDatabase.recipes.select.where(_.url eqs ?).prepare()

    val operation = for {
      truncate <- TestDatabase.recipes.truncate.future
      insertDone <- TestDatabase.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select, select2)

    operation.successful {
      case (items, items2) => {
        items shouldBe defined
        items.value shouldEqual recipe

        items2 shouldBe defined
        items2.value shouldEqual recipe
      }
    }
  }

  it should "serialise and execute a prepared select statement with the correct number of arguments" in {
    val recipe = gen[Recipe]

    val query = TestDatabase.recipes.select.where(_.url eqs ?).prepare()

    val operation = for {
      truncate <- TestDatabase.recipes.truncate.future
      insertDone <- TestDatabase.recipes.store(recipe).future()
      select <- query.bind(recipe.url).one()
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldEqual recipe
      }
    }
  }

  it should "serialise and execute a prepared statement with 2 arguments" in {
    val sample = gen[Article]
    val sample2 = gen[Article]
    val owner = gen[UUID]
    val category = gen[UUID]
    val category2 = gen[UUID]

    val query = TestDatabase.articlesByAuthor.select
      .where(_.author_id eqs ?)
      .and(_.category eqs ?)
      .prepare()

    val op = for {
      store <- TestDatabase.articlesByAuthor.store(owner, category, sample).future()
      store2 <- TestDatabase.articlesByAuthor.store(owner, category2, sample2).future()
      get <- query.bind(owner, category).one()
      get2 <- query.bind(owner, category2).one()
    } yield (get, get2)

    whenReady(op) {
      case (res, res2) => {
        res shouldBe defined
        res.value shouldEqual sample

        res2 shouldBe defined
        res2.value shouldEqual sample2
      }
    }
  }

  it should "serialise and execute a primitives prepared select statement with the correct number of arguments" in {
    val primitive = gen[Primitive]

    val query = TestDatabase.primitives.select.where(_.pkey eqs ?).prepare()

    val operation = for {
      truncate <- TestDatabase.primitives.truncate.future
      insertDone <- TestDatabase.primitives.store(primitive).future()
      select <- query.bind(primitive.pkey).one()
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldEqual primitive
      }
    }
  }

  if(session.v4orNewer) {
    it should "serialise and execute a primitives cassandra 2.2 prepared select statement with the correct number of arguments" in {
      val primitive = gen[PrimitiveCassandra22]

      val query = TestDatabase.primitivesCassandra22.select.where(_.pkey eqs ?).prepare()

      val operation = for {
        truncate <- TestDatabase.primitivesCassandra22.truncate.future
        insertDone <- TestDatabase.primitivesCassandra22.store(primitive).future()
        select <- query.bind(primitive.pkey).one()
      } yield select

      operation.successful {
        items => {
          items shouldBe defined
          items.value shouldEqual primitive
        }
      }
    }
  }
}
