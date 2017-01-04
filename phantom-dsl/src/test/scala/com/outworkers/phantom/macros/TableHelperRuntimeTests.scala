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
package com.outworkers.phantom.macros

import java.util.UUID

import com.outworkers.phantom.PhantomSuite
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl.context

class TableHelperRuntimeTests extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.tableTypeTuple.insertSchema()
  }

  it should "automatically generate an extractor for a tuple type" in {
    val sample = (gen[UUID], gen[String], gen[String])

    val chain = for {
      store <- database.tableTypeTuple.store(sample).future()
      find <- database.tableTypeTuple.findById(sample._1)
    } yield find

    whenReady(chain) { res =>
      res shouldBe defined
      res shouldEqual sample
    }
  }

}
