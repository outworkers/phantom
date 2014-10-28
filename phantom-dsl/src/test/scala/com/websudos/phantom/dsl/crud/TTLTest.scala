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
package com.websudos.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.twitter.util.Duration
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{Primitive, Primitives}
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class TTLTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  it should "expire inserted records after 2 seconds" in {
    val row = gen[Primitive]
    val test = Primitives.insert
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
        .ttl(2)
        .future() flatMap {
          _ =>  Primitives.select.where(_.pkey eqs row.pkey).one()
        }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row
        Thread.sleep(Duration.fromSeconds(3).inMillis)
        val test2 = Primitives.select.where(_.pkey eqs row.pkey).one()
        test2 successful {
          expired => {
            expired.isEmpty shouldEqual true
          }
        }
      }
    }
  }

  it should "expire inserted records after 2 seconds with Twitter Futures" in {
    val row = gen[Primitive]
    val test = Primitives.insert
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
      .ttl(2)
      .execute() flatMap {
      _ =>  Primitives.select.where(_.pkey eqs row.pkey).get()
    }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row
        Thread.sleep(Duration.fromSeconds(3).inMillis)
        val test2 = Primitives.select.where(_.pkey eqs row.pkey).get
        test2 successful {
          expired => {
            expired.isEmpty shouldEqual true
          }
        }
      }
    }
  }
}
