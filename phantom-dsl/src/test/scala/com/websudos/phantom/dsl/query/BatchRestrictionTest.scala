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
package com.websudos.phantom.dsl.query

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{Recipes, Recipe}
import com.websudos.util.testing._

class BatchRestrictionTest extends FlatSpec with Matchers {

  val s = Recipes
  val b = BatchStatement
  val d = new DateTime

  it should "not allow using Select queries in a batch" in {
    "BatchStatement().add(Primitives.select)" shouldNot compile
  }

  it should "not allow using a primary key in a conditional clause" in {
    """Recipes.update.where(_.url eqs "someUrl").modify(_.name setTo "test").onlyIf(_.id eqs secondary)""" shouldNot compile
  }

  it should "not allow using SelectWhere queries in a batch" in {
    "BatchStatement().add(Primitives.select.where(_.pkey eqs gen[String]))" shouldNot compile
  }

  it should "not allow using Truncate queries in a batch" in {
    "BatchStatement().add(Primitives.truncate)" shouldNot compile
  }

  it should "not allow using Create queries in a batch" in {
    "BatchStatement().add(Primitives.create)" shouldNot compile
  }

  it should "allow setting a timestamp on a Batch query" in {
    val url = gen[String]
    "BatchStatement().timestamp(gen[DateTime].getMillis).add(Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).timestamp(gen[DateTime].getMillis))" should compile
  }

  it should "allow setting a timestamp on an Update query" in {
    val url = gen[String]
    "Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).timestamp(gen[DateTime].getMillis)" should compile
  }

  it should "allow setting a timestamp on a Compare-and-Set Update query" in {
    val url = gen[String]
    "Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).onlyIf(_.description eqs Some(url)).timestamp(gen[DateTime].getMillis)" should compile
  }

  it should "allow using a timestamp on an Insert query" in {
    val sample = gen[Recipe]
    "Recipes.insert.value(_.url, sample.url).value(_.description, sample.description).timestamp(gen[DateTime].getMillis)" should compile
  }

}
