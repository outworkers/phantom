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
package com.outworkers.phantom.finagle.query.prepared

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.finagle._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class PreparedDeleteQueryTest extends PhantomSuite with TwitterFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.createSchema()
    database.articlesByAuthor.createSchema()
  }

  it should "execute a prepared delete query" in {
    val recipe = gen[Recipe]

    val query = database.recipes.delete.where(_.url eqs ?).prepare()

    val chain = for {
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      delete <- query.bind(recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterDelete) =>
      initial shouldBe defined
      initial.value shouldEqual recipe
      afterDelete shouldBe empty
    }
  }

  it should "execute an asynchronous prepared delete query" in {
    val recipe = gen[Recipe]

    val chain = for {
      query <- database.recipes.delete.where(_.url eqs ?).prepareAsync()
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      delete <- query.bind(recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterDelete) =>
      initial shouldBe defined
      initial.value shouldEqual recipe
      afterDelete shouldBe empty
    }
  }

  it should "correctly execute a prepared delete query with 2 bound values" in {
    val (author, cat, article) = gen[(UUID, UUID, Article)]

    val query = database.articlesByAuthor.delete
      .where(_.category eqs ?)
      .and(_.author_id eqs ?)
      .prepare()

    val chain = for {
      store <- database.articlesByAuthor.store(author, cat, article).future()
      get <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
      delete <- query.bind(cat, author).future()
      get2 <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterDelete) =>
      initial shouldBe defined
      initial.value shouldEqual article
      afterDelete shouldBe empty
    }
  }

  it should "correctly execute an asynchronous prepared delete query with 2 bound values" in {
    val (author, cat, article) = gen[(UUID, UUID, Article)]

    val chain = for {
      query <- database.articlesByAuthor.delete.where(_.category eqs ?).and(_.author_id eqs ?).prepareAsync()
      store <- database.articlesByAuthor.store(author, cat, article).future()
      get <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
      delete <- query.bind(cat, author).future()
      get2 <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterDelete) =>
      initial shouldBe defined
      initial.value shouldEqual article
      afterDelete shouldBe empty
    }
  }

  it should "not compile invalid arguments sent to a store method" in {
    val (author, cat, article) = gen[(UUID, UUID, Article)]

    "database.articlesByAuthor.store(author, cat, article, cat)" shouldNot compile
  }

  it should "not compile invalid argument orders sent to store method" in {
    val (author, cat, article) = gen[(UUID, UUID, Article)]

    "database.articlesByAuthor.store(article, cat, author)" shouldNot compile
  }
}
