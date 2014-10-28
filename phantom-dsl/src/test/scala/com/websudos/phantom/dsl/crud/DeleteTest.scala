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

import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class DeleteTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  "Delete" should "work fine, when deleting the whole row" in {
    val row = gen[Primitive]
    val rcp =  Primitives.insert
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
      .value(_.bi, row.bi).future()

    val result = rcp flatMap {
      _ => {
        for {
          inserted <- Primitives.select.fetch
          delete <- Primitives.delete.where(_.pkey eqs row.pkey).future()
          deleted <- Primitives.select.where(_.pkey eqs row.pkey).one
        } yield (inserted, deleted)
      }
    }

    result successful {
      r => {
        r._1 contains row shouldEqual true
        r._2.isEmpty shouldEqual true
      }
    }
  }

  "Delete" should "work fine with Twitter Futures, when deleting the whole row" in {
    val row = gen[Primitive]
    val rcp =  Primitives.insert
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
      .value(_.bi, row.bi).execute()

    val result = rcp flatMap {
      _ => {
        for {
          inserted <- Primitives.select.collect()
          delete <- Primitives.delete.where(_.pkey eqs row.pkey).execute()
          deleted <- Primitives.select.where(_.pkey eqs row.pkey).get
        } yield (inserted.contains(row), deleted.isEmpty)
      }
    }

    result successful {
      r => {
        r._1 shouldEqual true
        r._2 shouldEqual true
      }
    }
  }
}
