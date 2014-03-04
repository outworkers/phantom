package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.finagle.Implicits._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ Recipe, Recipes }

class ListOperatorsTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "listoperators"


  it should "append an item to a list" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.uid eqs id).modify(_.ingredients append "test").future()
      select <- Recipes.select(_.ingredients).where(_.uid eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items shouldBe "test" :: recipe.ingredients
      }
    }
  }

  it should "append several items to a list" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- insert
      update <- Recipes.update.where(_.uid eqs id).modify(_.ingredients appendAll appendable).future()
      select <- Recipes.select(_.ingredients).where(_.uid eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items shouldBe appendable ::: recipe.ingredients
      }
    }
  }

}
