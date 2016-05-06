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
package com.websudos.phantom.builder.query.db.ordering

import com.datastax.driver.core.Session
import com.twitter.util.{Future => TwitterFuture}
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.batch.BatchQuery
import com.websudos.phantom.builder.Unspecified
import com.websudos.phantom.builder.query.db.ordering.TimeSeriesTest._
import com.websudos.phantom.builder.query.prepared._
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._

import scala.concurrent.{Future => ScalaFuture}

class TimeSeriesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.timeSeriesTable.insertSchema()
  }

  it should "fetch records in natural order for a descending clustering order" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- TestDatabase.timeSeriesTable.truncate.future()
      insert <- addRecordsToBatch(records).future()
      chunks <- TestDatabase.timeSeriesTable.select.limit(limit).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in natural order for a descending clustering order with prepared statements" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val query = TestDatabase.timeSeriesTable.select
      .p_where(_.id eqs ?)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- TestDatabase.timeSeriesTable.truncate.future()
      insert <- addRecordsToBatch(records).future()
      chunks <- query.bind(TestDatabase.timeSeriesTable.testUUID).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in ascending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- TestDatabase.timeSeriesTable.truncate.future()
      insert <- addRecordsToBatch(records).future()
      chunks <- {
        TestDatabase.timeSeriesTable.select
          .where(_.id eqs TestDatabase.timeSeriesTable.testUUID)
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

    val query = TestDatabase.timeSeriesTable.select
      .p_where(_.id eqs ?)
      .orderBy(_.timestamp.asc)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- TestDatabase.timeSeriesTable.truncate.future()
      insert <- addRecordsToBatch(records).future()
      chunks <- query.bind(TestDatabase.timeSeriesTable.testUUID).fetch()
    } yield chunks

    verifyResults(chain, records.take(limit))
  }

  it should "fetch records in descending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- TestDatabase.timeSeriesTable.truncate.future()
      insert <- addRecordsToBatch(records).future()
      chunks <- TestDatabase.timeSeriesTable.select
        .where(_.id eqs TestDatabase.timeSeriesTable.testUUID)
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

  def addRecordsToBatch(
    records: Seq[TimeSeriesRecord]
  )(
    implicit space: KeySpace,
    session: Session
  ): BatchQuery[Unspecified] = {

    records.foldLeft(Batch.unlogged) {
      (b, record) => {
        b.add(TestDatabase.timeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }
  }
}
