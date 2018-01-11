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

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.query.bugs.UserSchema
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class SelectTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.primitives.createSchema()
  }

  "Selecting the whole row" should "work fine" in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.store(row).future()
      b <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield b

    whenReady(chain) { res =>
      res.value shouldEqual row
    }
  }

  it should "allow directly applying SelectOps on a select projection with no clauses" in {
    val row = gen[UserSchema]

    val chain = for {
      store <- database.userSchema.truncate().future()
      store <- database.userSchema.store(row).future()
      res <- database.userSchema.select.one()
    } yield res

    whenReady(chain) { res =>
      res.value shouldEqual row
    }
  }
}
