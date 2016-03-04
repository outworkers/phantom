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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.{dsl, PhantomSuite}
import com.websudos.phantom.tables.{TimeUUIDRecord, TestDatabase}
import org.joda.time.DateTime
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class TimeUuidTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.timeuuidTable.insertSchema()
  }

  it should "be able to store and retrieve a time slice of records based on a combination of minTimeuuid and maxTimeuuid" in {

    val start = new dsl.DateTime().plusMinutes(-60)
    val end = new dsl.DateTime().plusMinutes(60)

    val id = UUIDs.timeBased()
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String],
      new DateTime(UUIDs.unixTimestamp(id))
    )

    val minuteOffset = start.plusMinutes(-1).timeuuid()
    val secondOffset = start.plusSeconds(-15).timeuuid()

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(minuteOffset))
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(secondOffset))
    )

    val chain = for {
      empty <- TestDatabase.timeuuidTable.truncate().future()
      store <- TestDatabase.timeuuidTable.store(record).future()
      store2 <- TestDatabase.timeuuidTable.store(record1).future()
      store3 <- TestDatabase.timeuuidTable.store(record2).future()
      get <- TestDatabase.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id <= maxTimeuuid(end))
        .and(_.id >= minTimeuuid(start))
        .fetch()

      get2 <- TestDatabase.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id <= maxTimeuuid(end))
        .and(_.id >= minTimeuuid(start.plusMinutes(-2)))
        .fetch()
    } yield (get, get2)

    whenReady(chain) {
      case (res, res2) => {

        res should contain(record)

        info("Should not contain record with a timestamp 1 minute before the selection window")
        res should not contain record1

        info("Should not contain record with a timestamp 15 seconds before the selection window")
        res should not contain record2

        info("Should contain all elements if we expand the selection window by 1 minute")
        res2 should contain (record)
        res2 should contain (record1)
        res2 should contain (record2)
      }
    }
  }

  ignore should "not retrieve anything for a mismatched selection time window" in {

    val start = new dsl.DateTime().plusMinutes(-60)
    val end = new dsl.DateTime().plusMinutes(60)

    val id = UUIDs.timeBased()
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String],
      new DateTime(UUIDs.unixTimestamp(id))
    )

    val minuteOffset = start.plusMinutes(-1).timeuuid()
    val secondOffset = start.plusSeconds(-15).timeuuid()

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(minuteOffset))
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(secondOffset))
    )

    val chain = for {
      empty <- TestDatabase.timeuuidTable.truncate().future()
      store <- TestDatabase.timeuuidTable.store(record).future()
      store2 <- TestDatabase.timeuuidTable.store(record1).future()
      store3 <- TestDatabase.timeuuidTable.store(record2).future()
      get <- TestDatabase.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id >= minTimeuuid(start.plusMinutes(-3)))
        .and(_.id <= maxTimeuuid(end.plusMinutes(-2)))
        .fetch()
    } yield get

    whenReady(chain) {
      case res => res.size shouldEqual 0
    }
  }

}
