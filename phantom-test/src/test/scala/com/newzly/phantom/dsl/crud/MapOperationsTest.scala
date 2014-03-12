package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ Recipe, Recipes }


class MapOperationsTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "listoperators"


  it should "support a single item map put operation" in {
    Recipes.insertSchema

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
        Console.println(s"${items.mkString(" ")}")
        items.isDefined shouldBe true
        items.get shouldBe props + item
      }
    }
  }

  it should "support a multiple item map put operation" in {
    Recipes.insertSchema

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
        Console.println(s"${items.mkString(" ")}")
        items.isDefined shouldBe true
        items.get shouldBe props ++ mapItems
      }
    }
  }
}
