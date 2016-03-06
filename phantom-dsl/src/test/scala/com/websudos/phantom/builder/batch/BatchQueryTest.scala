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
package com.websudos.phantom.builder.batch

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{JodaRow, TestDatabase}
import com.websudos.util.testing._
import org.joda.time.DateTime

class BatchQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitivesJoda.insertSchema()
  }

  it should "correctly execute a chain of INSERT queries" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = Batch.logged.add(statement1).add(statement2).add(statement3)

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

  it should "correctly execute a chain of INSERT queries with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = Batch.logged.add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- TestDatabase.primitivesJoda.truncate.execute()
      batchDone <- batch.execute()
      count <- TestDatabase.primitivesJoda.select.count.get()
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
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

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

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

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
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo  row2.bi)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.future()
      s3 <- statement2.future()
      b <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
      deleted <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row3.pkey).one()
    } yield (updated, deleted)

    w successful {
      res => {
        res._1.value shouldEqual row2
        res._2 shouldNot be (defined)
      }
    }
  }

  it should "correctly execute a batch query with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.execute()
      s3 <- statement2.execute()
      b <- batch.execute()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).get()
      deleted <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row3.pkey).get()
    } yield (updated, deleted)

    w successful {
      case (initial, deleted) => {
        initial shouldBe defined
        initial.value shouldEqual row2
        deleted.isEmpty shouldEqual true
      }
    }
  }

  ignore should "prioritise batch updates in a last first order" in {
    val row = gen[JodaRow]

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1)
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.execute()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).get()
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

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1)
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
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

    val last = new DateTime()
    val last1 = last.withDurationAdded(100, 5)
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1.timestamp(last.getMillis))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last1.getMillis))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.future()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res shouldEqual defined
        res.value.int shouldEqual (row.int + 15)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp with Twitter futures" in {
    val row = gen[JodaRow]

    val last = new DateTime()
    val last1 = last.withDurationAdded(100, 5)
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = TestDatabase.primitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1.timestamp(last))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last1.getMillis))
      .add(TestDatabase.primitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.execute()
      updated <- TestDatabase.primitivesJoda.select.where(_.pkey eqs row.pkey).get()
    } yield updated

    chain.successful {
      res => {
        res shouldEqual true
        res.value.int shouldEqual (row.int + 15)
      }
    }
  }
}
