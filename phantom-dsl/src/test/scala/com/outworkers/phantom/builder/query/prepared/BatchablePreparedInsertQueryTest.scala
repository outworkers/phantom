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
import com.outworkers.phantom.tables.{Recipe, TestDatabase}
import com.outworkers.util.samplers._

class BatchablePreparedInsertQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = TestDatabase.recipes.createSchema()
  }

  it should "serialize an prepared batch query" in {
    val sample1 = gen[Recipe]
    val sample2 = gen[Recipe]

    val query = database.recipes.insert
      .p_value(_.uid, ?)
      .p_value(_.url, ?)
      .p_value(_.servings, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.description, ?)
      .p_value(_.lastcheckedat, ?)
      .p_value(_.props, ?)
      .prepare()

    def storePrepared(recipe: Recipe): ExecutablePreparedQuery = query.bind(
      recipe.uid,
      recipe.url,
      recipe.servings,
      recipe.ingredients,
      recipe.description,
      recipe.lastCheckedAt,
      recipe.props
    )

    val exec1 = storePrepared(sample1)
    val exec2 = storePrepared(sample2)


    val chain = for {
      truncate <- database.recipes.truncate.future()
      store <- Batch.unlogged.add(exec1, exec2).future()
      get <- database.recipes.select.fetch()
    } yield get

    whenReady(chain) { res =>
      res should contain theSameElementsAs Seq(sample1, sample2)
    }
  }
}
