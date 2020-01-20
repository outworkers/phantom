/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class ListOperatorsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.recipes.createSchema()
  }

  it should "store items in a list in the same order" in {
    val recipe = gen[Recipe]

    val operation = for {
      _ <- database.recipes.truncate.future
      _ <- database.recipes.store(recipe).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients
    }
  }

  it should "store the same list size in Cassandra as it does in Scala" in {
    val recipe = gen[Recipe]

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients
      items.value should have size recipe.ingredients.size.toLong
    }
  }

  it should "append an item to a list" in {
    val recipe = gen[Recipe]
    val appendable = gen[ShortString].value

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients ::: List(appendable)
    }
  }

  it should "append an item to a list using prepared queries" in {
    val recipe = gen[Recipe]
    val appendable = gen[ShortString].value

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients append ?)
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(List(appendable), recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients ::: List(appendable)
    }
  }

  it should "append several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = genList[String]()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients ::: appendable
    }
  }

  it should "append several items to a list using prepared statements" in {
    val recipe = gen[Recipe]

    val appendable = genList[String]()

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients append ?)
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(appendable, recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients ::: appendable
    }
  }

  it should "prepend an item to a list" in {
    val recipe = gen[Recipe]
    val value = gen[ShortString].value

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend value).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual List(value) ::: recipe.ingredients
    }
  }

  it should "prepend an item to a list using prepared statements" in {
    val recipe = gen[Recipe]
    val value = gen[ShortString].value

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients prepend ?)
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(List(value), recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual List(value) ::: recipe.ingredients
    }
  }

  it should "prepend several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = genList[String]()

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend appendable).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual prependedValues ::: recipe.ingredients
    }
  }

  it should "remove an item from a list" in {
    val list = genList[String]()
    val droppable = list.headOption.value
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.ingredients discard droppable)
        .future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual list.tail
    }
  }

  it should "remove multiple items from a list" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.tail).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual List(list.head)
    }
  }


  it should "remove multiple items from a list using prepared statements" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients discard ?)
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(list.tail, recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual List(list.head)
    }
  }

  it should "set a 0 index inside a List" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val updatedValue = gen[ShortString].value

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, updatedValue)).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.headOption.value shouldEqual updatedValue
    }
  }

  it should "set a 0 index inside a List using prepared statements" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val updatedValue = gen[ShortString].value

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients setIdx (0, ?))
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(updatedValue, recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.headOption.value shouldEqual updatedValue
    }
  }

  it should "set a prepared index inside a List using prepared statements" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val updatedValue = gen[ShortString].value

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.ingredients setIdx (?, ?))
      .prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(0, updatedValue, recipe.url).future())
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.headOption.value shouldEqual updatedValue
    }
  }


  it should "set the third index inside a List" in {
    val recipe = gen[Recipe]

    val updatedValue = gen[ShortString].value

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.ingredients setIdx (3, updatedValue)).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value(3) shouldEqual updatedValue
    }
  }
}
