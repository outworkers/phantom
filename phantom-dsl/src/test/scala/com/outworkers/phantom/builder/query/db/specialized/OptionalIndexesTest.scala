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

import com.datastax.driver.core.exceptions.InvalidQueryException
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.OptionalSecondaryRecord
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

class OptionalIndexesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.optionalIndexesTable.createSchema()
  }

  it should "store a record and then retrieve it using an optional index" in {
    val sample = gen[OptionalSecondaryRecord].copy(secondary = Some(gen[Int]))

    val chain = for {
      store <- database.optionalIndexesTable.store(sample).future()
      select <- database.optionalIndexesTable.findById(sample.id)
      select2 <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
    } yield (select, select2)

    whenReady(chain) { case (byId, byIndex) =>
      byId shouldBe defined
      byId.value shouldEqual sample

      byIndex shouldBe defined
      byIndex.value shouldEqual sample
    }
  }

  it should "not be able to delete records by their secondary index" in {
    val sample = gen[OptionalSecondaryRecord].copy(secondary = Some(gen[Int]))

    val chain = for {
      store <- database.optionalIndexesTable.store(sample).future()
      select <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
      delete <- database.optionalIndexesTable.delete.where(_.secondary eqs sample.secondary.value).future()
      select2 <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
    } yield (select, select2)

    whenReady(chain.failed) { r =>
      r shouldBe an [InvalidQueryException]
    }
  }

}
