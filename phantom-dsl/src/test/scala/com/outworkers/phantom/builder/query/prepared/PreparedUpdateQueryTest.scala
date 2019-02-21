/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
import com.outworkers.phantom.tables.Recipe
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.bugs.VerizonRecord
import com.outworkers.util.samplers._

class PreparedUpdateQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.createSchema()
    database.verizonSchema.createSchema()
  }

  it should "execute a prepared update query with a single argument bind" in {

    val updated = genOpt[ShortString].map(_.value)

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.description setTo ?)
      .prepare()

    val recipe = gen[Recipe]

    val chain = for {
      _ <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      _ <- query.bind(updated, recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterUpdate) =>

      initial shouldBe defined
      initial.value shouldEqual recipe

      afterUpdate shouldBe defined
      afterUpdate.value.url shouldEqual recipe.url
      afterUpdate.value.props shouldEqual recipe.props
      afterUpdate.value.ingredients shouldEqual recipe.ingredients
      afterUpdate.value.servings shouldEqual recipe.servings
      afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
      afterUpdate.value.uid shouldEqual recipe.uid
      afterUpdate.value.description shouldEqual updated
    }
  }

  it should "execute an asynchronous prepared update query with a single argument bind" in {
    val updated = genOpt[ShortString].map(_.value)

    val recipe = gen[Recipe]

    val chain = for {
      query <- database.recipes.update.where(_.url eqs ?).modify(_.description setTo ?).prepareAsync()
      _ <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      _ <- query.bind(updated, recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterUpdate) =>

      initial shouldBe defined
      initial.value shouldEqual recipe

      afterUpdate shouldBe defined
      afterUpdate.value.url shouldEqual recipe.url
      afterUpdate.value.props shouldEqual recipe.props
      afterUpdate.value.ingredients shouldEqual recipe.ingredients
      afterUpdate.value.servings shouldEqual recipe.servings
      afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
      afterUpdate.value.uid shouldEqual recipe.uid
      afterUpdate.value.description shouldEqual updated
    }
  }

  it should "execute a prepared update query with a three argument bind" in {

    val updated = genOpt[ShortString].map(_.value)
    val updatedUid = gen[UUID]

    val query = database.recipes.update
      .where(_.url eqs ?)
      .modify(_.description setTo ?)
      .and(_.uid setTo ?)
      .prepare()

    val recipe = gen[Recipe]

    val chain = for {
      _ <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      _ <- query.bind(updated, updatedUid, recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterUpdate) =>
      initial shouldBe defined
      initial.value shouldEqual recipe

      afterUpdate shouldBe defined
      afterUpdate.value.url shouldEqual recipe.url
      afterUpdate.value.props shouldEqual recipe.props
      afterUpdate.value.ingredients shouldEqual recipe.ingredients
      afterUpdate.value.servings shouldEqual recipe.servings
      afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
      afterUpdate.value.uid shouldEqual updatedUid
      afterUpdate.value.description shouldEqual updated
    }
  }

  it should "execute an async prepared update query with a three argument bind" in {

    val updated = genOpt[ShortString].map(_.value)
    val updatedUid = gen[UUID]

    val recipe = gen[Recipe]

    val chain = for {
      query <- database.recipes
        .update
        .where(_.url eqs ?)
        .modify(_.description setTo ?)
        .and(_.uid setTo ?)
        .prepareAsync()

      _ <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      _ <- query.bind(updated, updatedUid, recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) { case (initial, afterUpdate) =>
      initial shouldBe defined
      initial.value shouldEqual recipe

      afterUpdate shouldBe defined
      afterUpdate.value.url shouldEqual recipe.url
      afterUpdate.value.props shouldEqual recipe.props
      afterUpdate.value.ingredients shouldEqual recipe.ingredients
      afterUpdate.value.servings shouldEqual recipe.servings
      afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
      afterUpdate.value.uid shouldEqual updatedUid
      afterUpdate.value.description shouldEqual updated
    }
  }

  it should "correctly chain type parameters in conditional non-async prepared update clauses" in {
    val sample = gen[VerizonRecord].copy(isDeleted = true)
    val sample2 = gen[VerizonRecord].copy(isDeleted = true)

    val bindable = db.verizonSchema.update
      .where(_.uid eqs ?)
      .modify(_.isdeleted setTo ?)
      .ifExists
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
      .prepare()

    val chain = for {
      _ <- db.verizonSchema.storeRecord(sample)
      _ <- db.verizonSchema.storeRecord(sample2)
      updated <- bindable.bind(false, sample.uid).future()
      res <- db.verizonSchema.select.where(_.uid eqs sample.uid).one()
    } yield (updated, res)

    whenReady(chain) { case (updated, res) =>
      res shouldBe defined
      res.value.isDeleted shouldBe false
    }
  }

  it should "correctly chain type parameters in conditional async prepared update clauses" in {
    val sample = gen[VerizonRecord].copy(isDeleted = true)
    val sample2 = gen[VerizonRecord].copy(isDeleted = true)

    val chain = for {
      _ <- db.verizonSchema.storeRecord(sample)
      _ <- db.verizonSchema.storeRecord(sample2)
      _ <- db.verizonSchema.updateDeleteStatus.flatMap(_.bind(false, sample.uid).future())
      res <- db.verizonSchema.select.where(_.uid eqs sample.uid).one()
    } yield res

    whenReady(chain) { res =>
      res shouldBe defined
      res.value.isDeleted shouldBe false
    }
  }
}
