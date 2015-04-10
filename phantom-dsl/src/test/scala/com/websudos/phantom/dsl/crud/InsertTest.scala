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
package com.websudos.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.phantom.tables.{MyTest, MyTestRow, Primitive, Primitives, Recipe, Recipes, TestRow, TestTable}
import com.websudos.util.testing._

class InsertTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

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
      multi <- Primitives.select.fetch
    } yield (one.get === row, multi contains row)

    chain successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }

  }

  "Insert" should "work fine for primitives columns with twitter futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- Primitives.store(row).execute()
      one <- Primitives.select.where(_.pkey eqs row.pkey).get
      multi <- Primitives.select.collect()
    } yield (one.get === row, multi contains row)

    chain successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    val row = gen[TestRow]

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .future() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).one
          multi <- TestTable.select.fetch
        }  yield (one.get === row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map and Twitter futures" in {
    val row = gen[TestRow]

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .execute() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).get
          multi <- TestTable.select.collect()
        }  yield (one, multi)
      }
    }
    rcp successful {
      res => {
        res._1.isDefined shouldEqual true
        res._1.get shouldEqual row

        res._2.contains(row) shouldEqual true
      }
    }
  }

  it should "work fine with Mix" in {
    val r = gen[Recipe]
    val rcp = Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.servings, r.servings)
        .value(_.last_checked_at, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased()).future() flatMap {
        _ => {
         Recipes.select.where(_.url eqs r.url).one
        }
      }

    rcp successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual r
      }
    }
  }

  it should "work fine with Mix and Twitter futures" in {
    val r = gen[Recipe]
    val rcp = Recipes.insert
      .value(_.url, r.url)
      .valueOrNull(_.description, r.description)
      .value(_.ingredients, r.ingredients)
      .valueOrNull(_.servings, r.servings)
      .value(_.last_checked_at, r.lastCheckedAt)
      .value(_.props, r.props)
      .value(_.uid, UUIDs.timeBased()).execute() flatMap {
      _ => {
        Recipes.select.where(_.url eqs r.url).get
      }
    }

    rcp successful {
      res => {
        res.get shouldEqual r
      }
    }
  }

  it should "support serializing/de-serializing empty lists " in {
    val row = gen[MyTestRow]
    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, List.empty[String])
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    f successful  {
      res =>
        res.isEmpty shouldEqual false
        res.get.stringlist.isEmpty shouldEqual true
    }
  }

  it should "support serializing/de-serializing empty lists with Twitter futures" in {
    val row = gen[MyTestRow]

    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, List.empty[String])
      .execute() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).get
    }

    f successful  {
      res =>
        res.isEmpty shouldEqual false
        res.get.stringlist.isEmpty shouldEqual true
    }
  }

  it should "support serializing/de-serializing to List " in {
    val row = gen[MyTestRow]

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.optionA, row.optionA)
      .value(_.stringlist, row.stringlist)
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    recipeF successful  {
      res => {
        res.isEmpty shouldEqual false
        res.get shouldEqual row
      }
    }
  }

  it should "support serializing/de-serializing to List with Twitter futures" in {
    val row = gen[MyTestRow]

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.optionA, row.optionA)
      .value(_.stringlist, row.stringlist)
      .execute() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).get
    }

    recipeF successful  {
      res => {
        res.isEmpty shouldEqual false
        res.get shouldEqual row
      }
    }
  }

}
