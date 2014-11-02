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

import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class TruncateTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Articles.insertSchema()
  }

  it should "truncate all records in a table" in {
    val article1 = gen[Article]
    val article2 = gen[Article]
    val article3 = gen[Article]
    val article4 = gen[Article]

    val result = for {
      truncateBefore <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.orderId, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.orderId, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.orderId, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.orderId, article4.order_id)
        .future()
      records <- Articles.select.fetch
      truncate <- Articles.truncate.future()
      records1 <- Articles.select.fetch
    } yield (records, records1)


    result successful {
      r => {
        r._1.size shouldEqual 4
        info (s"inserted exactly ${r._1.size} records")

        r._2.size shouldEqual 0
        info (s"got exactly ${r._2.size} records")
      }
    }
  }

  it should "truncate all records in a table with Twitter Futures" in {
    val article1 = gen[Article]
    val article2 = gen[Article]
    val article3 = gen[Article]
    val article4 = gen[Article]

    val result = for {
      truncateBefore <- Articles.truncate.execute()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.orderId, article1.order_id)
        .execute()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.orderId, article2.order_id)
        .execute()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.orderId, article3.order_id)
        .execute()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.orderId, article4.order_id)
        .execute()
      records <- Articles.select.collect()
      truncate <- Articles.truncate.execute()
      records1 <- Articles.select.collect()
    } yield (records, records1)


    result successful {
      r => {
        r._1.size shouldEqual 4
        info (s"inserted exactly ${r._1.size} records")

        r._2.size shouldEqual 0
        info (s"got exactly ${r._2.size} records")
      }
    }
  }
}
