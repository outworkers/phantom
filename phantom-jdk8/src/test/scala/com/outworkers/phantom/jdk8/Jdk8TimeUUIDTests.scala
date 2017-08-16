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
package com.outworkers.phantom.jdk8

import java.time.{OffsetDateTime, ZonedDateTime}

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TimeUUIDRecord
import com.outworkers.util.samplers._
import org.scalacheck.Gen

import scala.concurrent.Future

class Jdk8TimeUUIDTests extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeuuidTable.createSchema()
  }

  it should "be able to store and retrieve a time slice of records based on an OffsetDateTime" in {

    val interval = 60
    val now = OffsetDateTime.now()
    val start = now.plusMinutes(-interval)
    val end = now.plusMinutes(interval)
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String]
    )

    /**
      * Cassandra sometimes skews the timestamp of this date by exactly 1 milliseconds
      * for reasons beyond our understanding, which means the test is flaky unless this
      * list is added to make sure at least one of t, t minus 1 millisecond, or t plus 1 millisecond
      * is found in the expected list of records.
      */
    val recordList = record :: Nil

    val minuteOffset = start.plusMinutes(-1).timeuuid
    val secondOffset = start.plusSeconds(-15).timeuuid

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String]
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String]
    )

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      _ <- database.timeuuidTable.store(record1).future()
      _ <- database.timeuuidTable.store(record2).future()
      get <- database.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id <= maxTimeuuid(end))
        .and(_.id >= minTimeuuid(start))
        .fetch()

      get2 <- database.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id >= minTimeuuid(start.plusMinutes(-2)))
        .and(_.id <= maxTimeuuid(end))
        .fetch()
    } yield (get, get2)

    whenReady(chain) { case (res, res2) =>
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

  it should "be able to store and retrieve a time slice of records based on ZonedDateTime" in {

    val interval = 60
    val now = ZonedDateTime.now()
    val start = now.plusMinutes(-interval)
    val end = now.plusMinutes(interval)
    val user = UUIDs.random()

    val record = TimeUUIDRecord(
      user,
      UUIDs.timeBased(),
      gen[String]
    )

    /**
      * Cassandra sometimes skews the timestamp of this date by exactly 1 milliseconds
      * for reasons beyond our understanding, which means the test is flaky unless this
      * list is added to make sure at least one of t, t minus 1 millisecond, or t plus 1 millisecond
      * is found in the expected list of records.
      */
    val recordList = record :: Nil

    val minuteOffset = start.plusMinutes(-1).timeuuid
    val secondOffset = start.plusSeconds(-15).timeuuid

    val record1 = TimeUUIDRecord(
      user,
      minuteOffset,
      gen[String]
    )

    val record2 = TimeUUIDRecord(
      user,
      secondOffset,
      gen[String]
    )

    val chain = for {
      _ <- database.timeuuidTable.store(record).future()
      _ <- database.timeuuidTable.store(record1).future()
      _ <- database.timeuuidTable.store(record2).future()
      one <- database.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id <= maxTimeuuid(end))
        .and(_.id >= minTimeuuid(start))
        .fetch()

      one2 <- database.timeuuidTable.select
        .where(_.user eqs record.user)
        .and(_.id >= minTimeuuid(start.plusMinutes(-2)))
        .and(_.id <= maxTimeuuid(end))
        .fetch()
    } yield (one, one2)

    whenReady(chain) { case (res, res2) =>
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

  it should "not retrieve anything for a mismatched selection time window using ZonedDateTime" in {

    val intervalOffset = 60
    val now = ZonedDateTime.now()
    val start = now.plusSeconds(-intervalOffset)
    val user = UUIDs.random()

    // I will repent for my sins in the future, I'm sorry Ben.
    val records = genList[TimeUUIDRecord]()
      .map(_.copy(
        user = user,
        id = now.plusSeconds(
          Gen.choose(
            -intervalOffset,
            intervalOffset
          ).sample.get
        ).timeuuid)
      )

    val chain = for {
      _ <- Future.sequence(records.map(r => database.timeuuidTable.store(r).future()))
      one <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id >= minTimeuuid(start.plusSeconds(-3 * intervalOffset)))
        .and(_.id <= maxTimeuuid(start.plusSeconds(-2 * intervalOffset)))
        .fetch()
    } yield one

    whenReady(chain) { res =>
      res.size shouldEqual 0
    }
  }

  it should "not retrieve anything for a mismatched selection time window using OffsetDateTime" in {

    val intervalOffset = 60
    val now = OffsetDateTime.now()
    val start = now.plusSeconds(-intervalOffset)
    val user = UUIDs.random()

    // I will repent for my sins in the future, I'm sorry Ben.
    val records = genList[TimeUUIDRecord]()
      .map(_.copy(
        user = user,
        id = now.plusSeconds(
          Gen.choose(
            -intervalOffset,
            intervalOffset
          ).sample.get
        ).timeuuid)
      )

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      get <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id >= minTimeuuid(start.plusSeconds(-3 * intervalOffset)))
        .and(_.id <= maxTimeuuid(start.plusSeconds(-2 * intervalOffset)))
        .fetch()
    } yield get

    whenReady(chain) { res =>
      res.size shouldEqual 0
    }
  }

}
