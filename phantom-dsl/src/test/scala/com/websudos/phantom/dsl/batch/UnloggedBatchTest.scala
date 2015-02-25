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
package com.websudos.phantom.dsl.batch

import org.joda.time.DateTime
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{JodaRow, PrimitivesJoda}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class UnloggedBatchTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    PrimitivesJoda.insertSchema()
  }

  it should "get the correct count for batch queries" in {
    val row = gen[JodaRow]
    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row.pkey)
      .modify(_.intColumn setTo row.int)
      .and(_.timestamp setTo row.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row.pkey)

    val batch = UnloggedBatchStatement().add(statement3, statement4)

  }

  it should "serialize a multiple table batch query applied to multiple statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = UnloggedBatchStatement().add(statement3, statement4)
    batch.queryString shouldEqual s"BEGIN UNLOGGED BATCH UPDATE PrimitivesJoda SET intColumn=${row2.int},timestamp=${row2.bi.getMillis} WHERE pkey='${row2.pkey}';DELETE FROM PrimitivesJoda WHERE pkey='${row3.pkey}';APPLY BATCH;"
  }

  it should "serialize a multiple table batch query chained from adding statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = UnloggedBatchStatement().add(statement3).add(statement4)
    batch.queryString shouldEqual s"BEGIN UNLOGGED BATCH UPDATE PrimitivesJoda SET intColumn=${row2.int},timestamp=${row2.bi.getMillis} WHERE pkey='${row2.pkey}';DELETE FROM PrimitivesJoda WHERE pkey='${row3.pkey}';APPLY BATCH;"
  }

  it should "correctly execute a chain of INSERT queries" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = UnloggedBatchStatement().add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 3
      }
    }
  }

  it should "correctly execute a chain of INSERT queries with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = UnloggedBatchStatement().add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- PrimitivesJoda.truncate.execute()
      batchDone <- batch.execute()
      count <- PrimitivesJoda.count.get()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 3
      }
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement().add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 1
      }
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement().add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 1
      }
    }
  }

  it should "correctly execute an UPDATE/DELETE pair batch query" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo  row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = UnloggedBatchStatement().add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.future()
      s3 <- statement2.future()
      b <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
      deleted <- PrimitivesJoda.select.where(_.pkey eqs row3.pkey).one()
    } yield (updated, deleted)

    w successful {
      res => {
        res._1.isDefined shouldEqual true
        res._1.get shouldEqual row2

        res._2.isEmpty shouldEqual true
      }
    }
  }

  it should "correctly execute a batch query with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = UnloggedBatchStatement().add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.execute()
      s3 <- statement2.execute()
      b <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
      deleted <- PrimitivesJoda.select.where(_.pkey eqs row3.pkey).get()
    } yield (updated, deleted)

    w successful {
      res => {
        res._1.isDefined shouldEqual true
        res._1.get shouldEqual row2

        res._2.isEmpty shouldEqual true
      }
    }
  }

  ignore should "prioritise batch updates in a last first order" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement()
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 20)
      }
    }
  }

  ignore should "prioritise batch updates in a last first order with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement()
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 20)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp" in {
    val row = gen[JodaRow]

    val last = gen[DateTime]
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement()
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last.getMillis))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 15)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp with Twitter futures" in {
    val row = gen[JodaRow]

    val last = gen[DateTime]
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = UnloggedBatchStatement()
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last.getMillis))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 15)
      }
    }
  }
}
