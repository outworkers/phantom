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
package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Recipe
import com.outworkers.util.samplers._

class ConditionalQueriesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.create.ifNotExists().future().block(defaultScalaTimeout)
  }

  it should "update the record if the optional column based condition matches" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description).future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      initial shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }

  it should "execute an update when a list column is used a conditional clause" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is recipe.ingredients).future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      initial shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }

  it should "not execute the update when the list column in a conditional clause doesn't match" in {
    val recipe = gen[Recipe]
    val invalidMatch = genList[String](2)
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is invalidMatch).future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it shouldn't have updated the value")
      second.value.description shouldEqual recipe.description
    }
  }

  it should "not update the record if the optional column based condition doesn't match" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is updated).future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>

      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual recipe.description
    }
  }

  it should "execute an update with a multi-part CAS conditional query with no collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.uid is recipe.uid).future()

      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>

      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }

  it should "execute an update with a tri-part CAS conditional query with no collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.lastcheckedat is recipe.lastCheckedAt)
        .and(_.uid is recipe.uid).future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.ingredients is recipe.ingredients)
        .future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns and simple comparisons in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- database.recipes.store(recipe).future()
      select1 <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- database.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.uid is recipe.uid)
        .and(_.ingredients is recipe.ingredients)
        .future()
      select2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    whenReady(chain) { case (initial, second) =>
      info("The first record should not be empty")
      initial shouldBe defined

      info("And it should match the inserted values")
      initial.value.url shouldEqual recipe.url

      info("The updated record should not be empty")
      second shouldBe defined

      info("And it should contain the updated value of the uid")
      second.value.description shouldEqual updated
    }
  }
}
