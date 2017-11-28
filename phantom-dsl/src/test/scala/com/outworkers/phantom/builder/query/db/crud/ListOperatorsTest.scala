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
      truncate <- database.recipes.truncate.future
      insertDone <- database.recipes.store(recipe).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients
    }
  }

  it should "store the same list size in Cassandra as it does in Scala" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- database.recipes.store(recipe).future()
      select <- database.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.ingredients
      items.value should have size recipe.ingredients.size
    }
  }

  it should "append an item to a list" in {
    val recipe = gen[Recipe]
    val appendable = gen[ShortString].value

    val operation = for {
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
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
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
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
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend value).future()
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
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend appendable).future()
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
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard droppable).future()
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
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.tail).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
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
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, updatedValue)).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.headOption.value shouldEqual updatedValue
    }
  }

  it should "set the third index inside a List" in {
    val recipe = gen[Recipe]

    val updatedValue = gen[ShortString].value

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, updatedValue)).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value(3) shouldEqual updatedValue
    }
  }
}
