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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.builder.query.compilation

import com.websudos.phantom.builder.query.SerializationTest
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Recipe, TestDatabase}
import com.outworkers.util.testing._
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
