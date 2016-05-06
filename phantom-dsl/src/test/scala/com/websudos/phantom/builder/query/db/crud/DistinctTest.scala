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
package com.websudos.phantom.builder.query.db.crud

import java.util.UUID

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import org.joda.time.DateTime

class DistinctTest extends PhantomSuite {

  override def beforeAll(): Unit = {
      super.beforeAll()
      TestDatabase.tableWithCompoundKey.insertSchema()
  }

  it should "return distinct primary keys" in {
    val rows = List(
      StubRecord("a", UUID.nameUUIDFromBytes("1".getBytes)),
      StubRecord("b", UUID.nameUUIDFromBytes("1".getBytes)),
      StubRecord("c", UUID.nameUUIDFromBytes("2".getBytes)),
      StubRecord("d", UUID.nameUUIDFromBytes("3".getBytes))
    )

    val batch = rows.foldLeft(Batch.unlogged)((batch, row) => {
      batch.add(
        TestDatabase.tableWithCompoundKey.insert
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

    whenReady(chain) {
      res => {
        res should contain only (expectedResult: _*)
      }
    }
  }

  private[this] implicit def string2date(date: String): DateTime = new DateTime(date)
}
