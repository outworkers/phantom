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
package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.query.prepared.ListValue
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Recipe
import com.outworkers.util.samplers._

class InOperatorTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.recipes.createSchema()
  }

  it should "find a record with a in operator if the record exists" in {
    val recipe = gen[Recipe]

    val chain = for {
      done <- database.recipes.store(recipe).future()
      select <- database.recipes.select.where(_.url in List(recipe.url, gen[EmailAddress].value)).one()
    } yield select

    whenReady(chain) { res =>
      res.value.url shouldEqual recipe.url
    }
  }

  it should "find a record with an in operator if the record exists using a prepared clause" in {
    val recipe = gen[Recipe]

    val chain = for {
      done <- database.recipes.store(recipe).future()
      selectIn <- database.recipes.select.where(_.url in ?).prepareAsync()
      bindedIn <- selectIn.bind(ListValue(recipe.url, gen[ShortString].value)).one()
    } yield bindedIn

    whenReady(chain) { resIn =>
      resIn.value.url shouldEqual recipe.url
    }
  }

  it should "not find a record with a in operator if the record doesn't exists" in {
    val recipe = gen[Recipe]

    val chain = for {
      done <- database.recipes.store(recipe).future()
      select <- database.recipes.select.where(_.url in List(gen[EmailAddress].value)).one()
    } yield select

    whenReady(chain) { res =>
      res shouldBe empty
    }
  }

}
