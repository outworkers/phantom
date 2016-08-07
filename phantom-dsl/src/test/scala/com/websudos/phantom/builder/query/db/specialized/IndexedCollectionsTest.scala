/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.exceptions.{InvalidQueryException, SyntaxError}
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{TestDatabase, TestRow}
import com.outworkers.util.testing._

class IndexedCollectionsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    if (cassandraVersion.value >= Version.`2.1.0`) {
      database.indexedCollectionsTable.insertSchema()
    }

    if (cassandraVersion.value >= Version.`2.2.0`) {
      database.indexedEntriesTable.insertSchema()
    }
  }

  it should "store a record and retrieve it with a CONTAINS query on the SET" in {
    val record = gen[TestRow]

    val chain = for {
      store <- TestDatabase.indexedCollectionsTable.store(record).future()
      get <- TestDatabase.indexedCollectionsTable.select
        .where(_.setText contains record.setText.headOption.value)
        .fetch()
    } yield get

    if (cassandraVersion.value > Version.`2.1.0`) {
      whenReady(chain) {
        res => {
          res.nonEmpty shouldEqual true
          res should contain (record)
        }
      }
    } else {
      chain.failing[SyntaxError]
    }

  }

  it should "store a record and retrieve it with a CONTAINS query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- TestDatabase.indexedCollectionsTable.store(record).future()
      get <- TestDatabase.indexedCollectionsTable.select
        .where(_.mapTextToText contains record.mapTextToText.values.headOption.value)
        .fetch()
    } yield get

    if (cassandraVersion.value > Version.`2.1.0`) {
      whenReady(chain) {
        res => {
          res.nonEmpty shouldEqual true
          res should contain (record)
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS KEY query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- TestDatabase.indexedCollectionsTable.store(record).future()
      get <- TestDatabase.indexedCollectionsTable
        .select
        .where(_.mapIntToText containsKey record.mapIntToText.keys.headOption.value)
        .fetch()
    } yield get

    if (cassandraVersion.value > Version.`2.1.0`) {
      whenReady(chain) {
        res => {
          res.nonEmpty shouldEqual true
          res should contain (record)
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS ENTRY equals query on the map" in {
    val record = gen[TestRow].copy(mapIntToInt = Map(5 -> 10, 10 -> 15, 20 -> 25))

    val chain = for {
      store <- TestDatabase.indexedEntriesTable.store(record).future()
      result <- TestDatabase.indexedEntriesTable.select.where(_.mapIntToInt(20) eqs 25).fetch()
    } yield result

    if (cassandraVersion.value > Version.`2.2.0`) {
      whenReady(chain) {
        res => {
          res.nonEmpty shouldEqual true
          res should contain (record)
        }
      }
    } else {
      chain.failing[InvalidQueryException]
    }
  }

}
