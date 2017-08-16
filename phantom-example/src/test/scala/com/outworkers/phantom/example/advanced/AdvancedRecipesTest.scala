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
package com.outworkers.phantom.example.advanced

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.example.ExampleSuite
import com.outworkers.phantom.example.basics.Recipe
import com.outworkers.util.samplers._

class AdvancedRecipesTest extends ExampleSuite {

  it should "insert a new record in the table" in {
    val sample = gen[Recipe]

    val chain = for {
      _ <- database.AdvancedRecipes.store(sample).future()
      rec <- database.AdvancedRecipes.findById(sample.id)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "insert a new record in both tables using the pre-existing database method" in {
    val sample = gen[Recipe]

    val chain = for {
      _ <- database.store(sample)
      rec <- database.AdvancedRecipes.findById(sample.id)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "allow indexing recipes by title" in {
    val sample = gen[Recipe]

    val chain = for {
      _ <- database.AdvancedRecipesByTitle.store(sample.title -> sample.id).future()
      rec <- database.AdvancedRecipesByTitle.findRecipeByTitle(sample.title)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      val (title, id) = res.value

      title shouldEqual sample.title
      id shouldEqual sample.id
    }
  }
}
