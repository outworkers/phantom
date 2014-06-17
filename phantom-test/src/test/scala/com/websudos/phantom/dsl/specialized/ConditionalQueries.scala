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

import scala.concurrent.blocking
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{ Recipe, Recipes }
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

  it should "execute an update when a list column is used a conditional clause" in {

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
        second.get.description shouldEqual updated
      }
    }
  }

  it should "not execute the update when the list column in a conditional clause doesn't match" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()

    val invalidMatch = List("invalid1", "invalid2")
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
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.ingredients eqs invalidMatch).future()
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

        info("And it shouldn't have updated the value")
        second.get.description shouldNot equal(updated)
      }
    }
  }

  it should "execute an update when a list column is used a conditional clause with Twitter Futures" in {

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
        second.get.description shouldEqual updated
      }
    }
  }

  it should "not execute the update when the list column in a conditional clause doesn't match with Twitter Futures" in {
    val recipe = Recipe.sample
    val id = UUIDs.timeBased()

    val invalidMatch = List("invalid1", "invalid2")
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
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.ingredients eqs invalidMatch).execute()
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

        info("And it shouldn't have updated the value")
        second.get.description shouldNot equal(updated)
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

  it should "execute an update with a multi-part CAS conditional query with no collection columns in the CAS part" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs recipe.description).and(_.uid eqs id).future()
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

  it should "execute an update with a multi-part CAS conditional query with no collection columns in the CAS part with Twitter Futures" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.description setTo updated).onlyIf(_.description eqs recipe.description).and(_.uid eqs id).execute()
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

  it should "execute an update with a tri-part CAS conditional query with no collection columns in the CAS part" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description eqs recipe.description)
        .and(_.last_checked_at eqs recipe.lastCheckedAt)
        .and(_.uid eqs id).future()
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

  it should "execute an update with a tri-part CAS conditional query with no collection columns in the CAS part with Twitter Futures" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description eqs recipe.description)
        .and(_.last_checked_at eqs recipe.lastCheckedAt)
        .and(_.uid eqs id).execute()

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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns in the CAS part" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props eqs recipe.props)
        .and(_.ingredients eqs recipe.ingredients)
        .future()
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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns in the CAS part with Twitter Futures" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props eqs recipe.props)
        .and(_.ingredients eqs recipe.ingredients)
        .execute()
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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns and simple comparisons in the CAS part" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props eqs recipe.props)
        .and(_.uid eqs id)
        .and(_.ingredients eqs recipe.ingredients)
        .future()
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


  it should "execute an update with a dual-part CAS query with a mixture of columns with Twitter Futures" in {

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
      update <- Recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props eqs recipe.props)
        .and(_.uid eqs id)
        .and(_.ingredients eqs recipe.ingredients)
        .execute()
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

}
