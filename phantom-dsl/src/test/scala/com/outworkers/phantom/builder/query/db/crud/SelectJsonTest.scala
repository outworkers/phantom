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
package com.outworkers.phantom.builder.query.db.crud

import com.datastax.driver.core.exceptions.SyntaxError
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._
import net.liftweb.json.JsonParser

class SelectJsonTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "A JSON selection clause" should "select an entire row as JSON" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      b <- TestDatabase.primitives.select.json().where(_.pkey eqs row.pkey).one
    } yield b


    if (cassandraVersion.value >= Version.`2.2.0`) {
      chain successful {
        res => {
          res shouldBe defined
          val parsed = JsonParser.parse(res.value)
          parsed.children.size shouldEqual row.productArity
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  "A JSON selection clause" should "8 columns as JSON" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
        .json()
        .where(_.pkey eqs row.pkey).one()
    } yield get

    if (cassandraVersion.value >= Version.`2.2.0`) {
      chain successful {
        res => {
          res shouldBe defined
          val parsed = JsonParser.parse(res.value)
          parsed.children.size shouldEqual expected.productArity
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }
}