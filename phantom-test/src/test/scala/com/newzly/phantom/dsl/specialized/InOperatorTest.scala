package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Recipe, Recipes }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class InOperatorTest extends BaseTest {
  val keySpace = "in_operators_test"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Recipes.insertSchema()
    }
  }

  it should "find a record with a in operator if the record exists" in {
    val id = UUIDs.timeBased()
    val recipe = Recipe.sample
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
      select <- Recipes.select.where(_.url in List(recipe.url, Sampler.getAUniqueEmailAddress)).one()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe true
        res.get.url shouldEqual recipe.url
      }
    }
  }

  it should "find a record with a in operator if the record exists with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val recipe = Recipe.sample
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
      select <- Recipes.select.where(_.url in List(recipe.url, Sampler.getAUniqueEmailAddress)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe true
        res.get.url shouldEqual recipe.url
      }
    }
  }

  it should "not find a record with a in operator if the record doesn't exists" in {
    val id = UUIDs.timeBased()
    val recipe = Recipe.sample
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
      select <- Recipes.select.where(_.url in List(Sampler.getAUniqueEmailAddress)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe false
      }
    }
  }

  it should "not find a record with a in operator if the record doesn't exists with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val recipe = Recipe.sample
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
      select <- Recipes.select.where(_.url in List(Sampler.getAUniqueEmailAddress)).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldBe false
      }
    }
  }

}
