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
package com.outworkers.phantom.example

import java.util.UUID

import com.outworkers.phantom.PhantomBaseSuite
import com.outworkers.phantom.dsl.DatabaseProvider
import com.outworkers.phantom.example.advanced.RecipesDatabase
import com.outworkers.phantom.example.basics.Recipe
import com.outworkers.util.testing._
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import com.outworkers.phantom.dsl.context

trait RecipesDbProvider extends DatabaseProvider[RecipesDatabase] {
  override def database: RecipesDatabase = RecipesDatabase
}

trait ExampleSuite extends FlatSpec with PhantomBaseSuite with RecipesDbProvider with RecipesDatabase.Connector {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.create()
  }

  implicit object RecipeSampler extends Sample[Recipe] {
    override def sample: Recipe = Recipe(
      id = gen[UUID],
      name = gen[ShortString].value,
      title = gen[ShortString].value,
      author = gen[ShortString].value,
      description = gen[ShortString].value,
      ingredients = genList[String]().toSet,
      props = genMap[String](),
      timestamp = gen[DateTime]
    )
  }
}
