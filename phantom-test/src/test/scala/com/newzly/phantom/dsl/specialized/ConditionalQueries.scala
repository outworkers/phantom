package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import com.datastax.driver.core.exceptions.InvalidQueryException
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Recipe, Recipes }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest
import com.newzly.util.testing.Sampler


class ConditionalQueries extends BaseTest {
  val keySpace = "conditional_queries"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Recipes.insertSchema()
    }
  }

  it should "update the record if the optional column based condition matches" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).one()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs recipe.description).future()
      select2 <- Recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldEqual updated
      }
    }
  }

  it should "update the record if the optional column based condition matches with Twitter Futures" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).get()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs recipe.description).execute()
      select2 <- Recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldEqual updated
      }
    }
  }

  it should "throw an error when a list column is used a conditional clause" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).one()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.ingredients eqs recipe.ingredients).future()
      select2 <- Recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldEqual(updated)
      }
    }
  }

  it should "throw an error when a list column is used a conditional clause with Twitter Futures" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).get()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.ingredients eqs recipe.ingredients).execute()
      select2 <- Recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldEqual(updated)
      }
    }
  }

  it should "not update the record if the optional column based condition doesn't match" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .future()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).one()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs updated).future()
      select2 <- Recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldNot equal(updated)
      }
    }
  }

  it should "not update the record if the optional column based condition doesn't match when using Twitter Futures" in {

    val recipe = Recipe.sample
    val id = UUIDs.timeBased()
    val updated = Some(Sampler.getARandomString)

    val insert = Recipes.insert
      .value(_.uid, id)
      .value(_.url, recipe.url)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.last_checked_at, recipe.lastCheckedAt)
      .value(_.props, recipe.props)
      .execute()

    val chain = for {
      insert <- insert
      select1 <- Recipes.select.where(_.url eqs recipe.url).get()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs updated).execute()
      select2 <- Recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial.isDefined shouldEqual true

        info("And it should match the inserted values")
        initial.get.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second.isDefined shouldEqual true

        info("And it should contain the updated value of the uid")
        second.get.description shouldNot equal(updated)
      }
    }
  }
}
