/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Articles, Recipes }
import com.newzly.util.testing.Sampler

class QuerySerializationTest extends FlatSpec with Matchers with ParallelTestExecution {

  it should "compile a full select query" in {
    "Articles.select.where(_.id eqs UUIDs.timeBased())" should compile
  }

  it should "correctly serialize a full select query" in {
    val someId = UUIDs.timeBased()
    Articles.select.where(_.id eqs someId).qb.toString shouldBe s"SELECT * FROM articles WHERE id=$someId;"
  }

  it should "compile a single column partial select query" in {
    "Articles.select(_.id).where(_.id eqs UUIDs.timeBased())" should compile
  }

  it should "correctly serialize a single column partial select query" in {
    val someId = UUIDs.timeBased()
    Articles.select(_.id).where(_.id eqs someId).qb.toString shouldBe s"SELECT id FROM ${Articles.tableName} WHERE id=$someId;"
  }

  it should "compile a query to query condition clause" in {
    """Articles.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo "test").onlyIf(_.name eqs "update")""" should compile
  }

  it should "serialize a condition query to a query condition" in {
    val someId = UUIDs.timeBased()
    val query = Articles.update.where(_.id eqs someId).modify(_.name setTo "test").onlyIf(_.name eqs "update").qb.toString
    query shouldEqual s"UPDATE articles SET name='test' WHERE id=$someId IF name='update';"
  }

  it should "correctly serialize a 2 column partial select query" in {
    val someId = UUIDs.timeBased()
    Articles.select(_.id, _.name).where(_.id eqs someId).qb.toString shouldBe s"SELECT id,name FROM articles WHERE id=$someId;"
  }

  it should "correctly serialize a 3 column partial select query" in {
    val someId = Sampler.getARandomString
    Recipes.select(
      _.url,
      _.description,
      _.ingredients
    ).where(_.url eqs someId).qb.toString shouldBe s"SELECT url,description,ingredients FROM Recipes WHERE url='$someId';"
  }

}
