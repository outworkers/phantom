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
package com.outworkers.phantom.builder.primitives


import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Recipe
import com.outworkers.phantom.tables.bugs.NpeRecipe
import com.outworkers.util.samplers._

class PrimitiveColumnRecipesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.createSchema()
    database.collectionNpeTable.createSchema()
  }

  it should "insert a new record in the recipes table and retrieve it" in {
    val sample = gen[NpeRecipe]

    val chain = for {
      _ <- database.collectionNpeTable.storeRecord(sample)
      res <- database.collectionNpeTable.select.where(_.id eqs sample.id).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "update the author of a recipe" in {
    val sample = gen[Recipe]
    val newAuthor = Some(gen[ShortString].value)

    val chain = for {
      _ <- database.recipes.storeRecord(sample)
      res <- database.recipes.select.where(_.url eqs sample.url).one()
      _ <- database.recipes
        .update.where(_.url eqs sample.url)
        .modify(_.description setTo newAuthor)
        .future()
      res2 <- database.recipes.select.where(_.url eqs sample.url).one()
    } yield (res, res2)

    whenReady(chain) { case (res, res2) =>
      res shouldBe defined
      res.value shouldEqual sample
      res2 shouldBe defined
      res2.value shouldEqual sample.copy(description = newAuthor)
    }
  }

  it should "retrieve an empty ingredients set" in {
    import com.outworkers.util.macros.debug.Options.ShowTrees
    val sample = gen[NpeRecipe]

    val chain = for {
      _ <- database.collectionNpeTable.insert()
        .value(_.id, sample.id)
        .value(_.name, sample.name)
        .value(_.title, sample.title)
        .value(_.author, sample.author)
        .value(_.description, sample.description)
        .value(_.timestamp, sample.timestamp)
        .future()
      res <- database.collectionNpeTable.findRecipeById(sample.id)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value.ingredients shouldBe empty
      res.value.props shouldBe empty
    }
  }
}