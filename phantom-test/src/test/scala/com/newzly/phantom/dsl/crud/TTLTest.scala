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
package com.newzly.phantom.dsl.crud

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.tables.{ Primitive, Primitives }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest
import com.twitter.util.Duration

class TTLTest extends BaseTest {
  val keySpace: String = "TTLTest"

  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Primitives.insertSchema()
    }
  }

  it should "expire inserted records" in {
    val row = Primitive.sample
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
        .ttl(5)
        .future() flatMap {
          _ =>  Primitives.select.one
        }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get should be (row)
        Thread.sleep(Duration.fromSeconds(6).inMillis)
        val test2 = Primitives.select.one
        test2 successful {
          expired => {
            assert(expired.isEmpty)
          }
        }
      }
    }
  }

  it should "expire inserted records with Twitter Futures" in {
    val row = Primitive.sample
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
      .ttl(5)
      .execute() flatMap {
      _ =>  Primitives.select.get
    }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get should be (row)
        Thread.sleep(Duration.fromSeconds(6).inMillis)
        val test2 = Primitives.select.get
        test2 successful {
          expired => {
            assert(expired.isEmpty)
          }
        }
      }
    }
  }
}
