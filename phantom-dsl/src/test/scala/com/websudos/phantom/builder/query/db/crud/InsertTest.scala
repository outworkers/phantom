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
package com.websudos.phantom.builder.query.db.crud

import java.util.UUID

import com.websudos.phantom.DateTimeSerializer
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{MyTest, MyTestRow, Primitive, Primitives, Recipe, Recipes, TestRow, TestTable}
import com.websudos.phantom.testkit._
import com.websudos.util.lift.UUIDSerializer
import com.websudos.util.testing._
import net.liftweb.json.JsonAST.{JString, JValue}
import net.liftweb.json._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.websudos.phantom.builder.primitives.DefaultPrimitives

import scala.util.control.NonFatal

class InsertTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  implicit val formats = net.liftweb.json.DefaultFormats + new UUIDSerializer + new DateTimeSerializer

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
    TestTable.insertSchema()
    MyTest.insertSchema()
    Recipes.insertSchema()
  }

  "Insert" should "work fine for primitives columns" in {
    val row = gen[Primitive]

    val chain = for {
      store <- Primitives.store(row).future()
      one <- Primitives.select.where(_.pkey eqs row.pkey).one
    } yield one

    chain successful {
      res => {
        res shouldBe defined
      }
    }

  }

  "Insert" should "work fine for primitives columns with twitter futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- Primitives.store(row).execute()
      one <- Primitives.select.where(_.pkey eqs row.pkey).get
    } yield one

    chain successful {
      res => {
        res shouldBe defined
      }
    }
  }

  it should "insert strings with single quotes inside them and automatically escape them" in {
    val row = gen[TestRow].copy(key = "test'")

    val chain = for {
      store <- TestTable.store(row).future()
      one <- TestTable.select.where(_.key eqs row.key).one
    } yield one

    chain successful {
      res => {
        res.value shouldEqual row
      }
    }
  }

  it should "insert strings with single quotes inside them and automatically escape them with Twitter Futures" in {
    val row = gen[TestRow].copy(key = "test'")

    val chain = for {
      store <- TestTable.store(row).execute()
      one <- TestTable.select.where(_.key eqs row.key).get
    } yield one

    chain successful {
      res => {
        res.value shouldEqual row
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    val row = gen[TestRow]

    val chain = for {
      store <- TestTable.store(row).future()
      one <- TestTable.select.where(_.key eqs row.key).one
    } yield one

    chain successful {
      res => {
        res.value shouldEqual row
      }
    }
  }

  it should "work fine with List, Set, Map and Twitter futures" in {
    val row = gen[TestRow]

    val chain = for {
      store <- TestTable.store(row).execute()
      one <- TestTable.select.where(_.key eqs row.key).get
    } yield one

    chain successful {
      res => {
        res.value shouldEqual row
      }
    }
  }

  it should "work fine with a mix of collection types in the table definition" in {
    val recipe = gen[Recipe]

    val chain = for {
      store <- Recipes.store(recipe).future()
      get <- Recipes.select.where(_.url eqs recipe.url).one
    } yield get

    chain successful {
      res => {
        res.isDefined shouldEqual true
        res.get.url shouldEqual recipe.url
        res.get.description shouldEqual recipe.description
        res.get.props shouldEqual recipe.props
        res.get.lastCheckedAt shouldEqual recipe.lastCheckedAt
        res.get.ingredients shouldEqual recipe.ingredients
        res.get.servings shouldEqual recipe.servings
      }
    }
  }

  it should "work fine with a mix of collection types in the table definition and Twitter futures" in {
    val recipe = gen[Recipe]

    val chain = for {
      store <- Recipes.store(recipe).execute()
      get <- Recipes.select.where(_.url eqs recipe.url).get
    } yield get

    chain successful {
      res => {
        res.isDefined shouldEqual true
        res.get.url shouldEqual recipe.url
        res.get.description shouldEqual recipe.description
        res.get.props shouldEqual recipe.props
        res.get.lastCheckedAt shouldEqual recipe.lastCheckedAt
        res.get.ingredients shouldEqual recipe.ingredients
        res.get.servings shouldEqual recipe.servings
      }
    }
  }

  it should "support serializing/de-serializing empty lists " in {
    val row = gen[MyTestRow].copy(stringlist = List.empty)

    val chain = for {
      store <- MyTest.store(row).future()
      get <- MyTest.select.where(_.key eqs row.key).one
    } yield get

    chain successful  {
      res => {
        res.value shouldEqual row
        res.value.stringlist.isEmpty shouldEqual true
      }
    }
  }

  it should "support serializing/de-serializing empty lists with Twitter futures" in {
    val row = gen[MyTestRow].copy(stringlist = List.empty)

    val chain = for {
      store <- MyTest.store(row).execute()
      get <- MyTest.select.where(_.key eqs row.key).get
    } yield get

    chain successful  {
      res => {
        res.value shouldEqual row
        res.value.stringlist.isEmpty shouldEqual true
      }
    }
  }

  it should "support serializing/de-serializing to List " in {
    val row = gen[MyTestRow]

    val chain = for {
      store <- MyTest.store(row).future()
      get <- MyTest.select.where(_.key eqs row.key).one
    } yield get

    chain successful  {
      res => {
        res.value shouldEqual row
      }
    }
  }

  it should "support serializing/de-serializing to List with Twitter futures" in {
    val row = gen[MyTestRow]

    val chain = for {
      store <- MyTest.store(row).future()
      get <- MyTest.select.where(_.key eqs row.key).one
    } yield get

    chain successful  {
      res => {
        res.value shouldEqual row
      }
    }
  }


  it should "serialize a JSON clause as the insert part" in {
    val sample = gen[Recipe]

    Console.println(pretty(render(Extraction.decompose(sample))))

    val chain = for {
      store <- Recipes.insert.json(compactRender(Extraction.decompose(sample))).future()
      get <- Recipes.select.where(_.url eqs sample.url).one()
    } yield get


    Console.println("Cassandra version: " + cassandraVersion.toString)

    if (cassandraVersion >= Version.`2.2.0`) {
      whenReady(chain) {
        res => {
          res shouldBe defined
          res.value shouldEqual sample
        }
      }
    } else {
      chain.failing[Exception]
    }
  }

}
