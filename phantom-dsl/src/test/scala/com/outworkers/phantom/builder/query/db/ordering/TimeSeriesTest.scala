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

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

import scala.concurrent.Future

class TimeSeriesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.timeSeriesTable.createSchema()
  }

  def genSequentialRecords(number: Int, ref: UUID = gen[UUID]): Seq[TimeSeriesRecord] = {
    val durationOffset = 1000

    (1 to number).map { i =>
      val record = gen[TimeSeriesRecord]
      record.copy(
        id = ref,
        timestamp = record.timestamp.withDurationAdded(durationOffset, i))
    }
  }

  it should "fetch records in natural order for a descending clustering order" in {
    val number = 10
    val limit = 5

    val records = genSequentialRecords(number)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- database.timeSeriesTable.storeRecords(records)
      chunks <- database.timeSeriesTable.select.limit(limit).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in natural order for a descending clustering order with prepared statements" in {
    val number = 10
    val limit = 5

    val ref = gen[UUID]
    val records = genSequentialRecords(number, ref)

    val query = database.timeSeriesTable.select
      .where(_.id eqs ?)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- database.timeSeriesTable.storeRecords(records)
      chunks <- query.bind(ref).fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  it should "fetch records in ascending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val ref = gen[UUID]
    val records = genSequentialRecords(number, ref)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- database.timeSeriesTable.storeRecords(records)
      chunks <- database.timeSeriesTable.select
        .where(_.id eqs ref)
        .orderBy(_.timestamp.asc)
        .limit(limit)
        .fetch()
    } yield chunks

    verifyResults(chain, records.take(limit))
  }

  it should "fetch records in ascending order for a descending clustering order using prepared statements" in {
    val number = 10
    val limit = 5

    val ref = gen[UUID]
    val records = genSequentialRecords(number, ref)

    val query = database.timeSeriesTable.select
      .where(_.id eqs ?)
      .orderBy(_.timestamp.asc)
      .limit(limit)
      .prepare()

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- database.timeSeriesTable.storeRecords(records)
      chunks <- query.bind(ref).fetch()
    } yield chunks

    verifyResults(chain, records.take(limit))
  }

  it should "fetch records in descending order for a descending clustering order using order by clause" in {
    val number = 10
    val limit = 5

    val ref = gen[UUID]
    val records = genSequentialRecords(number, ref)

    val chain = for {
      truncate <- database.timeSeriesTable.truncate.future()
      insert <- database.timeSeriesTable.storeRecords(records)
      chunks <- database.timeSeriesTable.select
        .where(_.id eqs ref)
        .orderBy(_.timestamp.descending)
        .limit(limit)
        .fetch()
    } yield chunks

    verifyResults(chain, records.reverse.take(limit))
  }

  def verifyResults(futureResults: Future[Seq[TimeSeriesRecord]], expected: Seq[TimeSeriesRecord]): Unit = {
    whenReady(futureResults) { results =>
      results should contain theSameElementsAs expected
    }
  }
}
