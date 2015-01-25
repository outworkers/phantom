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
