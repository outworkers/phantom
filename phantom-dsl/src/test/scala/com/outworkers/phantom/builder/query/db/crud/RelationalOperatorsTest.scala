/*
 * Copyright 2013-2016 Websudos, Limited.
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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.twitter.util.{Future => TwitterFuture}
import com.outworkers.phantom.builder.query.db.ordering.TimeSeriesTest
import com.outworkers.phantom.builder.query.prepared._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._
import org.slf4j.LoggerFactory

import scala.concurrent.{Future => ScalaFuture}

class RelationalOperatorsTest extends PhantomSuite {
  val logger = LoggerFactory.getLogger(this.getClass)

  val numRecords = 100
  val records: Seq[TimeSeriesRecord] = TimeSeriesTest.genSequentialRecords(numRecords)

  override def beforeAll(): Unit = {
    super.beforeAll()

    database.timeSeriesTable.insertSchema()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      inserts <- TimeSeriesTest.storeRecords(records)
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

    val expected =  records.filter(r => r.timestamp.isAfter(minTimestamp) && r.timestamp.isBefore(maxTimestamp))
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
    val expected =  records.filter(r => r.timestamp.isAfter(minTimestamp) && r.timestamp.isBefore(maxTimestamp))
    verifyResults(futureResults, expected)
  }

  def verifyResults(futureResults: ScalaFuture[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    futureResults.successful { results =>
      results.toSet shouldEqual expected.toSet
    }
  }

  def verifyResults(futureResults: TwitterFuture[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    futureResults.successful { results =>
      results.toSet shouldEqual expected.toSet
    }
  }
}
