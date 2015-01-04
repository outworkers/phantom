/*
 * Copyright 2014 websudos ltd.
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
package com.websudos.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class InsertCasTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
    TestTable.insertSchema()
    MyTest.insertSchema()
    Recipes.insertSchema()
  }

  "Standard inserts" should "create multiple database entries" in {
    val row = gen[Primitive]
    val insert =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)

    val chain = for {
      truncate <- Primitives.truncate.future()
      insert1 <- insert.future()
      insert2 <- insert.future()
      insert3 <- insert.future()
      insert4 <- insert.future()
      insert5 <- insert.future()
      one <- Primitives.select.where(_.pkey eqs row.pkey).one
      multi <- Primitives.select.where(_.pkey eqs row.pkey).fetch()
      count <- Primitives.count.one()
    } yield (one, count, multi)

    chain successful {
      res => {
        info("The one query should return a record")
        res._1.isDefined shouldEqual true

        info("And the record should equal the inserted record")
        res._1.get shouldEqual row

        info("And the count should be present")
        res._2.isDefined shouldEqual true

        info("And it should be one after a single insertion.")
        res._2.get shouldEqual 1L

        info("And only one record should be retrieved from a range fetch")
        res._3.size shouldEqual 1
      }
    }
  }


  "Conditional inserts" should "not create duplicate database entries" in {
    val row = gen[Primitive]
    val insert =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
      .ifNotExists()

    val chain = for {
      truncate <- Primitives.truncate.future()
      insert1 <- insert.future()
      insert2 <- insert.future()
      insert3 <- insert.future()
      insert4 <- insert.future()
      insert5 <- insert.future()
      one <- Primitives.select.where(_.pkey eqs row.pkey).one
      multi <- Primitives.select.where(_.pkey eqs row.pkey).fetch()
      count <- Primitives.count.one()
    } yield (one, count, multi)

    chain successful {
      res => {
        info("The one query should return a record")
        res._1.isDefined shouldEqual true

        info("And the record should equal the inserted record")
        res._1.get shouldEqual row

        info("And the count should be present")
        res._2.isDefined shouldEqual true

        info("And it should be one after a single insertion.")
        res._2.get shouldEqual 1L

        info("And only one record should be retrieved from a range fetch")
        res._3.size shouldEqual 1
      }
    }
  }

  "Conditional inserts" should "not create duplicate database entries with Twitter Futures" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[Primitive]
    val insert =  Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
      .ifNotExists()

    val chain = for {
      truncate <- Primitives.truncate.execute()
      insert1 <- insert.execute()
      insert2 <- insert.execute()
      insert3 <- insert.execute()
      insert4 <- insert.execute()
      insert5 <- insert.execute()
      one <- Primitives.select.where(_.pkey eqs row.pkey).get
      multi <- Primitives.select.where(_.pkey eqs row.pkey).collect()
      count <- Primitives.count.get()
    } yield (one, count, multi)

    chain successful {
      res => {
        info("The one query should return a record")
        res._1.isDefined shouldEqual true

        info("And the record should equal the inserted record")
        res._1.get shouldEqual row

        info("And the count should be present")
        res._2.isDefined shouldEqual true

        info("And it should be one after a single insertion.")
        res._2.get shouldEqual 1L

        info("And only one record should be retrieved from a range fetch")
        res._3.size shouldEqual 1
      }
    }
  }
}
