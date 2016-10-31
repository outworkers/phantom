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
package com.outworkers.phantom.builder.query.db.ordering

import com.datastax.driver.core.Session
import com.outworkers.phantom.PhantomSuite
import com.twitter.util.{Future => TwitterFuture}
import com.outworkers.phantom.builder.query.db.ordering.TimeSeriesTest._
import com.outworkers.phantom.builder.query.prepared._
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

import scala.concurrent.{Future => ScalaFuture}

class TimeSeriesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeSeriesTable.insertSchema()
  }

  it should "fetch records in natural order for a descending clustering order" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- storeRecords(records)
      chunks <- database.timeSeriesTable.select.limit(limit).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in natural order for a descending clustering order with prepared statements" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val query = database.timeSeriesTable.select
      .where(_.id eqs ?)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- storeRecords(records)
      chunks <- query.bind(database.timeSeriesTable.testUUID).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in ascending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- storeRecords(records)
      chunks <- {
        database.timeSeriesTable.select
          .where(_.id eqs database.timeSeriesTable.testUUID)
          .orderBy(_.timestamp.asc)
          .limit(limit)
          .fetch()
      }
    } yield chunks

    verifyResults(chain, records.take(limit))
  }

  it should "fetch records in ascending order for a descending clustering order using prepared statements" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val query = database.timeSeriesTable.select
      .where(_.id eqs ?)
      .orderBy(_.timestamp.asc)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- storeRecords(records)
      chunks <- query.bind(database.timeSeriesTable.testUUID).fetch()
    } yield chunks

    verifyResults(chain, records.take(limit))
  }

  it should "fetch records in descending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- storeRecords(records)
      chunks <- database.timeSeriesTable.select
        .where(_.id eqs database.timeSeriesTable.testUUID)
        .orderBy(_.timestamp.descending)
        .limit(limit)
        .fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  def verifyResults(futureResults: ScalaFuture[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    futureResults.successful { results =>
      results shouldEqual expected
    }
  }

  def verifyResults(futureResults: TwitterFuture[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    futureResults.successful { results =>
      results shouldEqual expected
    }
  }
}

object TimeSeriesTest {
  def genSequentialRecords(number: Int): Seq[TimeSeriesRecord] = {
    val durationOffset = 1000

    (1 to number).map { i =>
      val record = gen[TimeSeriesRecord]
      record.copy(
        id = TestDatabase.timeSeriesTable.testUUID,
        timestamp = record.timestamp.withDurationAdded(durationOffset, i))
    }
  }

  def storeRecords(
    records: Seq[TimeSeriesRecord]
  )(
    implicit space: KeySpace,
    session: Session
  ): ScalaFuture[Seq[ResultSet]] = {

    val futures = records map {
      record => {
        TestDatabase.timeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp)
          .future()
      }
    }
    ScalaFuture.sequence(futures)
  }
}
