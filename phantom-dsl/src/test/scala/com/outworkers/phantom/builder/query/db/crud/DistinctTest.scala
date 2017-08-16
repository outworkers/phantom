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

import java.util.UUID

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._

class DistinctTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.tableWithCompoundKey.createSchema()
  }

  it should "return distinct primary keys" in {
    val rows = List(
      StubRecord(UUID.nameUUIDFromBytes("1".getBytes), "a"),
      StubRecord(UUID.nameUUIDFromBytes("1".getBytes), "b"),
      StubRecord(UUID.nameUUIDFromBytes("2".getBytes), "c"),
      StubRecord(UUID.nameUUIDFromBytes("3".getBytes), "d")
    )

    val batch = rows.foldLeft(Batch.unlogged)((batch, row) => {
      batch.add(
        database.tableWithCompoundKey.insert
          .value(_.id, row.id)
          .value(_.second, UUID.nameUUIDFromBytes(row.name.getBytes))
          .value(_.name, row.name)
      )
    })

    val chain = for {
      truncate <- TestDatabase.tableWithCompoundKey.truncate.future()
      batch <- batch.future()
      list <- TestDatabase.tableWithCompoundKey.select(_.id).distinct.fetch
    } yield list

    val expectedResult = rows.filter(_.name != "b").map(_.id)

    whenReady(chain) { res =>
      res should contain only (expectedResult: _*)
    }
  }
}
