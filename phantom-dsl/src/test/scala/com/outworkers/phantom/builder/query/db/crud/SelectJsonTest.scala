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
import com.outworkers.util.samplers._
import org.json4s.native.JsonParser
import shapeless.Nat
import shapeless.syntax.std.tuple._

class SelectJsonTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.createSchema()
  }

  "A JSON selection clause" should "select an entire row as JSON" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.store(row).future()
      b <- database.primitives.select.json().where(_.pkey eqs row.pkey).one
    } yield b

    if (cassandraVersion.value >= Version.`2.2.0`) {
      whenReady(chain) { res =>
        res shouldBe defined
        val parsed = JsonParser.parse(res.value)
        parsed.children.size shouldEqual row.productArity
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [SyntaxError]
      }
    }
  }

  "A JSON selection clause" should "8 columns as JSON" in {
    val row = gen[PrimitiveRecord]
    val expected = row.take(Nat._8)

    val chain = for {
      store <- database.primitives.store(row).future()
      one <- database.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
        .json()
        .where(_.pkey eqs row.pkey).one()
    } yield one

    if (cassandraVersion.value >= Version.`2.2.0`) {
      whenReady(chain) { res =>
        res shouldBe defined
        val parsed = JsonParser.parse(res.value)
        parsed.children.size shouldEqual expected.productArity
      }
    } else {
      whenReady(chain.failed) { r =>
        r shouldBe an [SyntaxError]
      }
    }
  }
}
