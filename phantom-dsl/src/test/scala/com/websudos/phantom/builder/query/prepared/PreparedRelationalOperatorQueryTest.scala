/*
 * Copyright 2013-2016 Websudos, Limited.
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
package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class PreparedRelationalOperatorQueryTest extends PhantomSuite {

  lazy val logger = LoggerFactory.getLogger(this.getClass)

  val url = "low_calories.html"

  val recipes = for (i <- 1 to 100) yield {
    gen[Recipe].copy(calories = i, url = url)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    
    val insertFutures = recipes.map { recipe =>
      TestDatabase.recipes.store(recipe).future()
    }

    val futureInserts = Future.sequence(insertFutures)

    futureInserts.successful { inserts =>
      logger.debug(s"Added ${inserts.size} recipes")
    }
  }

  override def afterAll(): Unit = {
    val deleteFutures = recipes.map { recipe =>
      TestDatabase.recipes.delete
        .where(_.url eqs url)
        .and(_.calories eqs recipe.calories)
        .future()
    }

    val futureDeletes = Future.sequence(deleteFutures)

    futureDeletes.successful { deletes =>
      logger.debug(s"Removed ${deletes.size} recipes")
    }

    super.afterAll()
  }

  it should "support prepared statement with less than operator" in {
    val max: Long = 50

    val query = TestDatabase.recipes.select.p_where(_.url eqs ?).p_and(_.calories < ?).prepare()

    val futureResults = query.bind(url, max).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.calories should be < max
      }

      val lessThanRecipes = recipes.filter(_.calories < max)

      lessThanRecipes.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }

  it should "support prepared statement with less than or equal operator" in {
    val max: Long = 40

    val query = TestDatabase.recipes.select.p_where(_.url eqs ?).p_and(_.calories <= ?).prepare()

    val futureResults = query.bind(url, max).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.calories should be <= max
      }

      val lessThanRecipes = recipes.filter(_.calories <= max)

      lessThanRecipes.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }

  it should "support prepared statement with greater than operator" in {
    val min: Long = 60

    val query = TestDatabase.recipes.select.p_where(_.url eqs ?).p_and(_.calories > ?).prepare()

    val futureResults = query.bind(url, min).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.calories should be > min
      }

      val greaterThanRecipes = recipes.filter(_.calories > min)

      greaterThanRecipes.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }

  it should "support prepared statement with greater than or equal operator" in {
    val min: Long = 75

    val query = TestDatabase.recipes.select.p_where(_.url eqs ?).p_and(_.calories >= ?).prepare()

    val futureResults = query.bind(url, min).fetch()

    futureResults.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.calories should be >= min
      }

      val greaterThanRecipes = recipes.filter(_.calories >= min)

      greaterThanRecipes.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }

  it should "support prepared statement with less than and greater than operators" in {
    val min: Long = 10
    val max: Long = 40

    val query = TestDatabase.recipes.select
      .p_where(_.url eqs ?)
      .p_and(_.calories > ?)
      .p_and(_.calories < ?)
      .prepare()

    val operation = for {
      select <- query.bind(url, min, max).fetch()
    } yield select

    operation.successful { results =>
      results.foreach { result =>
        result.url shouldEqual url
        result.calories should be > min
        result.calories should be < max
      }

      val rangeResults = recipes.filter(r => r.calories > min && r.calories < max)

      rangeResults.foreach { recipe =>
        results should contain (recipe)
      }
    }
  }
}
