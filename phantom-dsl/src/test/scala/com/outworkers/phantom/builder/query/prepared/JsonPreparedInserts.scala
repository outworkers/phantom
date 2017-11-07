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
package com.outworkers.phantom.builder.query.prepared

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.bugs.NestedJsonRecord
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._

class JsonPreparedInserts extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.jsonPreparedTable.createSchema()
  }

  it should "insert a record into cassandra using a prepared query" in {
    val sample = gen[NestedJsonRecord]

    val chain = for {
      insert <- database.jsonPreparedTable.insertItem(sample)
      one <- database.jsonPreparedTable.findById(sample.id)
    } yield one

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

  it should "insert a record into cassandra using the default store method" in {
    val sample = gen[NestedJsonRecord]

    val chain = for {
      insert <- database.jsonPreparedTable.storeRecord(sample)
      one <- database.jsonPreparedTable.findById(sample.id)
    } yield one

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual sample
    }
  }

}
