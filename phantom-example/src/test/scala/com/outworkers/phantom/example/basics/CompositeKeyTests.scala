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

import com.outworkers.phantom.example.ExampleSuite
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._

class CompositeKeyTests extends ExampleSuite {

  it should "insert a new record in the table and retrieve it using the full primary key" in {
    val sample = gen[Recipe]

    val chain = for {
      store <- database.CompositeKeyRecipes.store(sample).future()
      rec <- database.CompositeKeyRecipes.findRecipeByIdAndName(sample.id, sample.name)
    } yield rec

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }
}
