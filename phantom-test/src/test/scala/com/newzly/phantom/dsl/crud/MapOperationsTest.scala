package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Recipe, Recipes }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest


class MapOperationsTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "map_operators"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Recipes.insertSchema()
    }
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
