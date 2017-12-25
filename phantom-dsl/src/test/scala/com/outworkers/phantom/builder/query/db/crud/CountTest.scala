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
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class CountTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.primitivesJoda.createSchema()
  }

  it should "retrieve a count of 0 if the table has been truncated" in {

    val chain = for {
      truncate <- database.primitivesJoda.truncate.future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual 0L
    }
  }

  it should "correctly retrieve a count of 1000" in {
    val limit = 100

    val rows = genList[JodaRow](limit)

    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(TestDatabase.primitivesJoda.store(row))
    })

    val chain = for {
      truncate <- database.primitivesJoda.truncate.future()
      batch <- batch.future()
      count <- database.primitivesJoda.select.count.one()
    } yield count

    whenReady(chain) { res =>
      res.value shouldEqual limit.toLong
    }
  }
}
