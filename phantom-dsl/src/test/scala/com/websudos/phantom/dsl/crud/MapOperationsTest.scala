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
package com.websudos.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.phantom.tables.{ Recipe, Recipes }
import com.websudos.util.testing.AsyncAssertionsHelper._

class MapOperationsTest extends PhantomCassandraTestSuite {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Recipes.insertSchema()
  }

  it should "support a single item map put operation" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val props = Map("test" -> "test_val", "test2" -> "test_val")
    val item = "test3" -> "test_val"

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.props put item).future()
      select <- Recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe props + item
      }
    }
  }

  it should "support a single item map put operation with Twitter futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()

    val props = Map("test" -> "test_val", "test2" -> "test_val")
    val item = "test3" -> "test_val"

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.props put item).execute()
      select <- Recipes.select(_.props).where(_.url eqs recipe.url).get
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe props + item
      }
    }
  }

  it should "support a multiple item map put operation" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()

    val props = Map("test" -> "test_val", "test2" -> "test_val")
    val mapItems = Map("test3" -> "test_val", "test4" -> "test_val")

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.props putAll mapItems).future()
      select <- Recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe props ++ mapItems
      }
    }
  }

  it should "support a multiple item map put operation with Twitter futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()

    val props = Map("test" -> "test_val", "test2" -> "test_val")
    val mapItems = Map("test3" -> "test_val", "test4" -> "test_val")

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, props)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.props putAll mapItems).execute()
      select <- Recipes.select(_.props).where(_.url eqs recipe.url).get
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe props ++ mapItems
      }
    }
  }
}
