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
package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class JodaDateTimeColumnTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitivesJoda.createSchema()
  }

  it should "correctly insert and extract a JodaTime date" in {
    val row = gen[JodaRow]

    val chain = for {
      store <- database.primitivesJoda.store(row).future()
      select <- database.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield select

    whenReady(chain) { res =>
      res.value.pkey shouldEqual row.pkey
      res.value.intColumn shouldEqual row.intColumn
      res.value.timestamp shouldEqual row.timestamp
    }
  }
}
