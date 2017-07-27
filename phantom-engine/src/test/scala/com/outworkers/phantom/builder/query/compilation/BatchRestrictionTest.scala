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
package com.outworkers.phantom.builder.query.compilation

import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{Recipe, TestDatabase}
import com.outworkers.util.samplers._
import org.joda.time.DateTime
import org.scalatest.FlatSpec

class BatchRestrictionTest extends FlatSpec with SerializationTest {

   val Recipes = TestDatabase.recipes
   val b = Batch.logged
   val d = new DateTime

  val str = gen[String]
  val Primitives = TestDatabase.primitives

   it should "not allow using Select queries in a batch" in {
     "Batch.logged.add(Primitives.select)" shouldNot compile
   }

   it should "not allow using a primary key in a conditional clause" in {
     """Recipes.update.where(_.url eqs "someUrl").modify(_.name setTo "test").onlyIf(_.id is secondary)""" shouldNot compile
   }

   it should "not allow using SelectWhere queries in a batch" in {
     "Batch.add(Primitives.select.where(_.pkey eqs gen[String]))" shouldNot compile
   }

   it should "not allow using Truncate queries in a batch" in {
     "Batch.add(Primitives.truncate)" shouldNot compile
   }

   it should "not allow using Create queries in a batch" in {
     "Batch.add(Primitives.create)" shouldNot compile
   }

   it should "allow setting a timestamp on a Batch query" in {
     val url = gen[String]
     "Batch.timestamp(gen[DateTime].getMillis).add(Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).timestamp(gen[DateTime].getMillis))" should compile
   }

   it should "allow setting a timestamp on an Update query" in {
     val url = gen[String]
     "Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).timestamp(gen[DateTime].getMillis)" should compile
   }

   ignore should "allow setting a timestamp on a Compare-and-Set Update query" in {
     val url = gen[String]
     "Recipes.update.where(_.url eqs url).modify(_.description setTo Some(url)).onlyIf(_.description is Some(url)).timestamp(gen[DateTime].getMillis)" should compile
   }

   it should "allow using a timestamp on an Insert query" in {
     val sample = gen[Recipe]
     "Recipes.insert.value(_.url, sample.url).value(_.description, sample.description).timestamp(gen[DateTime].getMillis)" should compile
   }

  it should "allow using Insert queries in a Batch statement" in {
    "Batch.logged.add(Primitives.insert)" should compile
  }

  it should " allow using an Insert.Value statement in a BatchStatement" in {
    "Batch.logged.add(Primitives.insert.value(_.long, 4L))" should compile
  }

  it should "allow using an Update.Assignments statement in a BatchStatement" in {
    "Batch.logged.add(Primitives.update.modify(_.long setTo 5L))" should compile
  }

  it should "allow using Update.Where queries in a BatchStatement" in {
    "Batch.logged.add(Primitives.update.where(_.pkey eqs gen[String]))" should compile
  }

  it should "allow using Conditional Update.Where queries in a BatchStatement" in {
    "Batch.logged.add(Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L))" should compile
  }

  it should " allow using Conditional Assignments queries in a BatchStatement" in {
    "Batch.logged.add(Primitives.update.where(_.pkey eqs gen[String]).modify(_.long setTo 10L).onlyIf(_.long is 5L))" should compile
  }

  it should " allow using Delete queries in a BatchStatement" in {
    "Batch.logged.add(Primitives.delete)" should compile
  }

  it should "Delete.Where queries in a BatchStatement" in {
    "Batch.logged.add(Primitives.delete)" should compile
  }
 }
