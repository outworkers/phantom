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
package com.outworkers.phantom.example.basics

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.example.ExampleSuite
import com.outworkers.util.samplers._

class SimpleRecipesTest extends ExampleSuite {

  it should "insert a new record in the recipes table and retrieve it" in {
    val sample = gen[Recipe]

    val chain = for {
      store <- database.Recipes.store(sample).future()
      res <- database.Recipes.findRecipeById(sample.id)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "retrieve the ingredients of a recipe" in {
    val sample = gen[Recipe]

    val chain = for {
      store <- database.Recipes.store(sample).future()
      res <- database.Recipes.findRecipeIngredients(sample.id)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample.ingredients
    }
  }

  it should "update the author of a recipe" in {
    val sample = gen[Recipe]
    val newAuthor = gen[ShortString].value

    val chain = for {
      store <- database.Recipes.store(sample).future()
      res <- database.Recipes.findRecipeById(sample.id)
      updateAuthor <- database.Recipes.updateRecipeAuthor(sample.id, newAuthor)
      res2 <- database.Recipes.findRecipeById(sample.id)
    } yield (res, res2)

    whenReady(chain) { case (res, res2) =>
      res shouldBe defined
      res.value shouldEqual sample
      res2 shouldBe defined
      res2.value shouldEqual sample.copy(author = newAuthor)
    }
  }

  it should "delete a recipe by its id" in {
    val sample = gen[Recipe]

    val chain = for {
      store <- database.Recipes.store(sample).future()
      res <- database.Recipes.findRecipeById(sample.id)
      updateAuthor <- database.Recipes.deleteRecipeById(sample.id)
      res2 <- database.Recipes.findRecipeById(sample.id)
    } yield (res, res2)

    whenReady(chain) { case (res, res2) =>
      res shouldBe defined
      res.value shouldEqual sample
      res2 shouldBe empty
    }
  }

  it should "retrieve a recipe by its autho using a secondary index" in {
    val sample = gen[Recipe]

    val chain = for {
      store <- database.SecondaryKeyRecipes.store(sample).future()
      res <- database.SecondaryKeyRecipes.findRecipeByAuthor(sample.author)
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

}
