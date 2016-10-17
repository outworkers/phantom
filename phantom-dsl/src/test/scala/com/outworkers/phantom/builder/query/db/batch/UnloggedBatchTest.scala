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
package com.outworkers.phantom.builder.query.db.batch

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{JodaRow, TestDatabase}
import com.outworkers.util.testing._
import org.joda.time.DateTime
import org.scalatest.time.SpanSugar._

class UnloggedBatchTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitivesJoda.create.ifNotExists().future().block(5.seconds)
  }

  it should "get the correct count for batch queries" in {
    val row = gen[JodaRow]
    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row.pkey)
      .modify(_.intColumn setTo row.intColumn)
      .and(_.timestamp setTo row.timestamp)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row.pkey)

    val batch = Batch.unlogged.add(statement3, statement4)

    batch.iterator.size shouldEqual 2

  }

  ignore should "serialize a multiple table batch query applied to multiple statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo row2.timestamp)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.unlogged.add(statement3, statement4)
    val expected = s"BEGIN UNLOGGED BATCH UPDATE phantom.TestDatabase.primitivesJoda " +
      s"SET intColumn = ${row2.intColumn}, timestamp = ${row2.timestamp.getMillis} " +
      s"WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.TestDatabase.primitivesJoda " +
      s"WHERE pkey = '${row3.pkey}'; APPLY BATCH;"

    batch.statement shouldEqual expected
  }

  ignore should "serialize a multiple table batch query chained from adding statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo row2.timestamp)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.unlogged.add(statement3).add(statement4)
    batch.queryString shouldEqual s"BEGIN UNLOGGED BATCH UPDATE phantom.TestDatabase.primitivesJoda SET intColumn = ${row2.intColumn}, timestamp = ${row2.timestamp.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.TestDatabase.primitivesJoda WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

  it should "correctly execute a chain of INSERT queries" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.intColumn)
      .value(_.timestamp, row2.timestamp)

    val statement3 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.intColumn)
      .value(_.timestamp, row3.timestamp)

    val batch = Batch.unlogged.add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- TestDatabase.primitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- TestDatabase.primitivesJoda.select.count.one()
    } yield count

    chain.successful {
      res => {
        res.value shouldEqual 3
      }
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts" in {
    val row = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.unlogged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- TestDatabase.primitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- TestDatabase.primitivesJoda.select.count.one()
    } yield count

    chain.successful {
      res => {
        res.value shouldEqual 1
      }
    }
  }

  it should "correctly execute an UPDATE/DELETE pair batch query" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.intColumn)
      .value(_.timestamp, row3.timestamp)

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo row2.timestamp)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.unlogged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.future()
      s3 <- statement2.future()
      b <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
      deleted <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row3.pkey).one()
    } yield (updated, deleted)

    w successful {
      case (res1, res2) => {
        res1.value shouldEqual row2
        res2 shouldBe empty
      }
    }
  }

  ignore should "prioritise batch updates in a last first order" in {
    val row = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.unlogged
      .add(statement1)
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.intColumn))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.intColumn + 10)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.intColumn + 15)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.intColumn + 20)))

    val chain = for {
      done <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.value.intColumn shouldEqual (row.intColumn + 20)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp" in {
    val row = gen[JodaRow]

    val last = gen[DateTime]
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.intColumn)
      .value(_.timestamp, row.timestamp)

    val batch = Batch.unlogged
      .add(statement1)
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.intColumn + 10)).timestamp(last.getMillis))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.intColumn + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.value.intColumn shouldEqual (row.intColumn + 15)
      }
    }
  }
}
