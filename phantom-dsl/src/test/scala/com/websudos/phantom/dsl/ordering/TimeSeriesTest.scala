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
package com.websudos.phantom.dsl.ordering

import scala.concurrent.duration._

import org.scalatest.concurrent.PatienceConfiguration

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class TimeSeriesTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    TimeSeriesTable.insertSchema()
  }

  it should "allow using naturally fetch the records in descending order for a descending clustering order" in {

    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) => {
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }

    val chain = for {
      truncate <- TimeSeriesTable.truncate.future()
      insert <- batch.future()
      chunks <- TimeSeriesTable.select.limit(5).fetch()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        val mapped = res.map(_.timestamp.getSecondOfDay)
        mapped.toList shouldEqual ts.reverse
    }
  }

  it should "allow using naturally fetch the records in descending order for a descending clustering order with Twitter Futures" in {
    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) => {
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }

    val chain = for {
      truncate <- TimeSeriesTable.truncate.execute()
      insert <- batch.execute()
      chunks <- TimeSeriesTable.select.limit(5).collect()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        val mapped = res.map(_.timestamp.getSecondOfDay)
        mapped.toList shouldEqual ts.reverse
    }
  }

  it should "allow fetching the records in ascending order for a descending clustering order using order by clause" in {
    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) => {
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }
    val chain = for {
      truncate <- TimeSeriesTable.truncate.future()
      insert <- batch.future()
      chunks <- TimeSeriesTable.select.limit(5).where(_.id eqs TimeSeriesTable.testUUID).orderBy(_.timestamp.asc).fetch()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)

        res.map(_.timestamp.getSecondOfDay).toList shouldEqual ts
    }
  }

  it should "allow fetching the records in ascending order for a descending clustering order using order by clause with Twitter Futures" in {
    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) => {
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }
    val chain = for {
      truncate <- TimeSeriesTable.truncate.execute()
      insert <- batch.execute()
      chunks <- TimeSeriesTable.select.limit(5).where(_.id eqs TimeSeriesTable.testUUID).orderBy(_.timestamp.asc).collect()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        res.map(_.timestamp.getSecondOfDay).toList shouldEqual ts
    }
  }

  it should "allow fetching the records in descending order for a descending clustering order using order by clause" in {
    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) =>
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
    }
    val chain = for {
      truncate <- TimeSeriesTable.truncate.future()
      insert <- batch.future()
      chunks <- TimeSeriesTable.select.limit(5).where(_.id eqs TimeSeriesTable.testUUID).orderBy(_.timestamp.desc).fetch()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        res.map(_.timestamp.getSecondOfDay).toList shouldEqual ts.reverse
    }
  }

  it should "allow fetching the records in descending order for a descending clustering order using order by clause with Twitter Futures" in {
    var i = 0

    val recordList = genList[TimeSeriesRecord](6).map(
      item => {
        i += 1
        item.copy(id = TimeSeriesTable.testUUID, timestamp = item.timestamp.withDurationAdded(500, i))
      })

    val batch = recordList.foldLeft(Batch.unlogged) {
      (b, record) =>
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
    }
    val chain = for {
      truncate <- TimeSeriesTable.truncate.execute()
      insert <- batch.execute()
      chunks <- TimeSeriesTable.select.limit(5).where(_.id eqs TimeSeriesTable.testUUID).orderBy(_.timestamp.desc).collect()
    } yield chunks

    chain.successful {
      res =>
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        res.map(_.timestamp.getSecondOfDay).toList shouldEqual ts.reverse
    }
  }

}
