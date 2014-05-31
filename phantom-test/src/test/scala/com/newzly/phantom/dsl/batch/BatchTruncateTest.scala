/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.dsl.batch

import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ JodaRow, PrimitivesJoda }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class BatchTruncateTest extends BaseTest {
  val keySpace = "batch_truncate_test"

  it should "truncate multiple tables in a single batch" in {

    val row = JodaRow.sample
    val row2 = JodaRow.sample.copy(pkey = row.pkey)
    val row3 = JodaRow.sample
    PrimitivesJoda.insertSchema()
    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)
    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)
    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .modify(_.timestamp setTo  row2.bi)
    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = new BatchStatement()
      .add(statement1)
      .add(statement2)
      .add(statement3)
      .add(statement4)

    val chain = for {
      b <- batch.execute()
      count <- PrimitivesJoda.count.get()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 1L
      }
    }

  }
}
