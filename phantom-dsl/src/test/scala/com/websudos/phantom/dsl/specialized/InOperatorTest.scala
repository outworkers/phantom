/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl.specialized

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{Recipe, Recipes}
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class InOperatorTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Recipes.insertSchema()
  }

  it should "find a record with a in operator if the record exists" in {
    val id = gen[UUID]
    val recipe = gen[Recipe]
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val chain = for {
      done <- insert
      select <- Recipes.select.where(_.url in List(recipe.url, gen[EmailAddress].address)).one()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe true
        res.get.url shouldEqual recipe.url
      }
    }
  }

  it should "find a record with a in operator if the record exists with Twitter Futures" in {
    val id = gen[UUID]
    val recipe = gen[Recipe]
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      done <- insert
      select <- Recipes.select.where(_.url in List(recipe.url, gen[EmailAddress].address)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe true
        res.get.url shouldEqual recipe.url
      }
    }
  }

  it should "not find a record with a in operator if the record doesn't exists" in {
    val id = gen[UUID]
    val recipe = gen[Recipe]
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      done <- insert
      select <- Recipes.select.where(_.url in List(gen[EmailAddress].address)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe false
      }
    }
  }

  it should "not find a record with a in operator if the record doesn't exists with Twitter Futures" in {
    val id = gen[UUID]
    val recipe = gen[Recipe]
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      done <- insert
      select <- Recipes.select.where(_.url in List(gen[EmailAddress].address)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe false
      }
    }
  }

}
