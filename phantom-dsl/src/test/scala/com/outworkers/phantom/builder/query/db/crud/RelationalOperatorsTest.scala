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

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class RelationalOperatorsTest extends PhantomSuite {
  val logger = LoggerFactory.getLogger(this.getClass)

  val numRecords = 100
  val records: Seq[TimeSeriesRecord] = genSequentialRecords(numRecords)


  def genSequentialRecords(number: Int, ref: UUID = gen[UUID]): Seq[TimeSeriesRecord] = {
    val durationOffset = 1000

    (1 to number).map { i =>
      val record = gen[TimeSeriesRecord]
      record.copy(
        id = ref,
        timestamp = record.timestamp.withDurationAdded(durationOffset, i))
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    database.timeSeriesTable.createSchema()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      inserts <- database.timeSeriesTable.storeRecords(records)
    } yield inserts

    whenReady(chain) { inserts =>
      logger.debug(s"Initialized table with $numRecords records")
    }
  }

  it should "fetch records using less than operator" in {
    val maxIndex = 50
    val maxTimestamp = records(maxIndex).timestamp

    val futureResults = database.timeSeriesTable.select
      .where(_.timestamp < maxTimestamp)
      .allowFiltering()
      .fetch()

    val expected = records.filter(_.timestamp.isBefore(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using less than operator with prepared statement" in {
    val maxIndex = 50
    val maxTimestamp = records(maxIndex).timestamp

    val query = database.timeSeriesTable.select
      .where(_.timestamp < ?)
      .allowFiltering()
      .prepare()

    val futureResults = query.bind(maxTimestamp).fetch()
    val expected = records.filter(_.timestamp.isBefore(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using less than or equal operator" in {
    val maxIndex = 40
    val maxTimestamp = records(maxIndex).timestamp

    val futureResults = database.timeSeriesTable.select
      .where(_.timestamp <= maxTimestamp)
      .allowFiltering()
      .fetch()

    val expected = records.filter(!_.timestamp.isAfter(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using less than or equal operator with prepared statement" in {
    val maxIndex = 40
    val maxTimestamp = records(maxIndex).timestamp

    val query = database.timeSeriesTable.select
      .where(_.timestamp <= ?)
      .allowFiltering()
      .prepare()

    val futureResults = query.bind(maxTimestamp).fetch()
    val expected = records.filter(!_.timestamp.isAfter(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using greater than operator" in {
    val minIndex = 60
    val minTimestamp = records(minIndex).timestamp

    val futureResults = database.timeSeriesTable.select
      .where(_.timestamp > minTimestamp)
      .allowFiltering()
      .fetch()

    val expected = records.filter(_.timestamp.isAfter(minTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using greater than operator with prepared statement" in {
    val minIndex = 60
    val minTimestamp = records(minIndex).timestamp

    val query = database.timeSeriesTable.select
      .where(_.timestamp > ?)
      .allowFiltering()
      .prepare()

    val futureResults = query.bind(minTimestamp).fetch()
    val expected = records.filter(_.timestamp.isAfter(minTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using greater than or equal operator" in {
    val minIndex = 75
    val minTimestamp = records(minIndex).timestamp

    val futureResults = database.timeSeriesTable.select
      .where(_.timestamp >= minTimestamp)
      .allowFiltering()
      .fetch()

    val expected = records.filter(!_.timestamp.isBefore(minTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using greater than or equal operator with prepared statement" in {
    val minIndex = 75
    val minTimestamp = records(minIndex).timestamp

    val query = database.timeSeriesTable.select
      .where(_.timestamp >= ?)
      .allowFiltering()
      .prepare()

    val futureResults = query.bind(minTimestamp).fetch()
    val expected = records.filter(!_.timestamp.isBefore(minTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using less than and greater than operators" in {
    val minIndex = 10
    val maxIndex = 40
    val minTimestamp = records(minIndex).timestamp
    val maxTimestamp = records(maxIndex).timestamp

    val futureResults = database.timeSeriesTable.select
      .where(_.timestamp > minTimestamp)
      .and(_.timestamp < maxTimestamp)
      .allowFiltering()
      .fetch()

    val expected = records.filter(r => r.timestamp.isAfter(minTimestamp) && r.timestamp.isBefore(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  it should "fetch records using less than and greater than operators with prepared statement" in {
    val minIndex = 10
    val maxIndex = 40
    val minTimestamp = records(minIndex).timestamp
    val maxTimestamp = records(maxIndex).timestamp

    val query = database.timeSeriesTable.select
      .where(_.timestamp > ?)
      .and(_.timestamp < ?)
      .allowFiltering()
      .prepare()

    val futureResults = query.bind(minTimestamp, maxTimestamp).fetch()
    val expected = records.filter(r => r.timestamp.isAfter(minTimestamp) && r.timestamp.isBefore(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  def verifyResults(futureResults: Future[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    whenReady(futureResults) { results =>
      results.toSet shouldEqual expected.toSet
    }
  }
}
