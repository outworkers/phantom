/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query.db.specialized

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{ Recipe, TestDatabase }
import com.websudos.util.testing._
import scala.concurrent.duration._

class ConditionalQueriesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.recipes.create.ifNotExists().future().block(defaultScalaTimeout)
  }

  it should "update the record if the optional column based condition matches" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description).future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      case (initial, second) => {
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
  }

  it should "update the record if the optional column based condition matches with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description).execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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
  }

  it should "execute an update when a list column is used a conditional clause" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is recipe.ingredients).future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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
  }

  it should "not execute the update when the list column in a conditional clause doesn't match" in {
    val recipe = gen[Recipe]
    val invalidMatch = genList[String](2)
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is invalidMatch).future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial shouldBe defined

        info("And it should match the inserted values")
        initial.value.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second shouldBe defined

        info("And it shouldn't have updated the value")
        second.value.description shouldNot equal(updated)
      }
    }
  }

  it should "execute an update when a list column is used a conditional clause with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is recipe.ingredients).execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "not execute the update when the list column in a conditional clause doesn't match with Twitter Futures" in {
    val recipe = gen[Recipe]

    val invalidMatch = List("invalid1", "invalid2")
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.ingredients is invalidMatch).execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial shouldBe defined

        info("And it should match the inserted values")
        initial.value.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second shouldBe defined

        info("And it shouldn't have updated the value")
        second.value.description shouldNot equal(updated)
      }
    }
  }

  it should "not update the record if the optional column based condition doesn't match" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is updated).future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial shouldBe defined

        info("And it should match the inserted values")
        initial.value.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second shouldBe defined

        info("And it should contain the updated value of the uid")
        second.value.description shouldNot equal(updated)
      }
    }
  }

  it should "not update the record if the optional column based condition doesn't match when using Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is updated).execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

        info("The first record should not be empty")
        initial shouldBe defined

        info("And it should match the inserted values")
        initial.value.url shouldEqual recipe.url

        info("The updated record should not be empty")
        second shouldBe defined

        info("And it should contain the updated value of the uid")
        second.value.description shouldNot equal(updated)
      }
    }
  }

  it should "execute an update with a multi-part CAS conditional query with no collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.uid is recipe.uid).future()

      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a multi-part CAS conditional query with no collection columns in the CAS part with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.uid is recipe.uid).execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a tri-part CAS conditional query with no collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.lastcheckedat is recipe.lastCheckedAt)
        .and(_.uid is recipe.uid).future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a tri-part CAS conditional query with no collection columns in the CAS part with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.description is recipe.description)
        .and(_.lastcheckedat is recipe.lastCheckedAt)
        .and(_.uid is recipe.uid).execute()

      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.ingredients is recipe.ingredients)
        .future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns in the CAS part with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.ingredients is recipe.ingredients)
        .execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

  it should "execute an update with a dual-part CAS conditional query with a mixture of collection columns and simple comparisons in the CAS part" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).future()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.uid is recipe.uid)
        .and(_.ingredients is recipe.ingredients)
        .future()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).one()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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


  it should "execute an update with a dual-part CAS query with a mixture of columns with Twitter Futures" in {

    val recipe = gen[Recipe]
    val updated = genOpt[String]

    val chain = for {
      insert <- TestDatabase.recipes.store(recipe).execute()
      select1 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url)
        .modify(_.description setTo updated)
        .onlyIf(_.props is recipe.props)
        .and(_.uid is recipe.uid)
        .and(_.ingredients is recipe.ingredients)
        .execute()
      select2 <- TestDatabase.recipes.select.where(_.url eqs recipe.url).get()
    } yield (select1, select2)

    chain.successful {
      res => {
        val initial = res._1
        val second = res._2

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

}
