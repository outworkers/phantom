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

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.{TestDatabase, TimeUUIDRecord}
import org.joda.time.{DateTime, DateTimeZone}
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._

class TimeUuidTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeuuidTable.insertSchema()
  }

  it should "be able to store and retrieve a time slice of records based on a combination of minTimeuuid and maxTimeuuid" in {

    val start = gen[DateTime].plusMinutes(-60)
    val end = gen[DateTime].plusMinutes(60)

    val id = UUIDs.timeBased()
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String],
      new DateTime(UUIDs.unixTimestamp(id), DateTimeZone.UTC)
    )

    /**
      * Cassandra sometimes skews the timestamp of this date by exactly 1 milliseconds
      * for reasons beyond our understanding, which means the test is flaky unless this
      * list is added to make sure at least one of t, t minus 1 millisecond, or t plus 1 millisecond
      * is found in the expected list of records.
      */
    val recordList = List(
      record,
      record.copy(timestamp = record.timestamp.plusMillis(1)),
      record.copy(timestamp = record.timestamp.plusMillis(2)),
      record.copy(timestamp = record.timestamp.plusMillis(3)),
      record.copy(timestamp = record.timestamp.plusMillis(-1)),
      record.copy(timestamp = record.timestamp.plusMillis(-2)),
      record.copy(timestamp = record.timestamp.plusMillis(-3))
    )

    val minuteOffset = start.plusMinutes(-1).timeuuid()
    val secondOffset = start.plusSeconds(-15).timeuuid()

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(minuteOffset), DateTimeZone.UTC)
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(secondOffset), DateTimeZone.UTC)
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
        .and(_.id >= minTimeuuid(start.plusMinutes(-2)))
        .and(_.id <= maxTimeuuid(end))
        .fetch()
    } yield (get, get2)

    whenReady(chain) {
      case (res, res2) => {

        info("At least one timestamp value, including potential time skewes, should be included here")
        recordList exists(res contains) shouldEqual true

        info("Should not contain record with a timestamp 1 minute before the selection window")
        res should not contain record1

        info("Should not contain record with a timestamp 15 seconds before the selection window")
        res should not contain record2

        info("Should contain all elements if we expand the selection window by 1 minute")
        res2.find(_.id == record.id) shouldBe defined
        res2.find(_.id == record1.id) shouldBe defined
        res2.find(_.id == record2.id) shouldBe defined
      }
    }
  }

  ignore should "not retrieve anything for a mismatched selection time window" in {

    val intervalOffset = 60

    val start = gen[DateTime].plusMinutes(intervalOffset * -1)
    val end = gen[DateTime].plusMinutes(intervalOffset)

    val id = UUIDs.timeBased()
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String],
      new DateTime(UUIDs.unixTimestamp(id), DateTimeZone.UTC)
    )

    val minuteOffset = start.plusMinutes(-1).timeuuid()
    val secondOffset = start.plusSeconds(-15).timeuuid()

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(minuteOffset), DateTimeZone.UTC)
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String],
      new DateTime(UUIDs.unixTimestamp(secondOffset), DateTimeZone.UTC)
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
      res => res.size shouldEqual 0
    }
  }

}
