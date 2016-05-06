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
package com.websudos.phantom.builder.query.db.batch

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase

class CounterBatchTest extends PhantomSuite {

  val x = TestDatabase.counterTableTest
  val y = TestDatabase.secondaryCounterTable

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.counterTableTest.insertSchema()
    TestDatabase.secondaryCounterTable.insertSchema()
  }

  it should "create a batch query to perform several updates in a single table" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))

    val chain = for {
      batched <- ft.future()
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value shouldEqual 2500
      }
    }
  }

  it should "create a batch query to update counters in several tables" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .future()

    val chain = for {
      batched <- ft
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- TestDatabase.secondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, updated) => {
        info("The first counter select should return the record")
        initial shouldBe defined
        info("and the counter value should match the sum of the +=s")
        initial.value shouldEqual 2500

        info("The second counter select should return the record")
        updated shouldBe defined
        info("and the counter value should match the sum of the +=s")
        updated.value shouldEqual 2500
      }
    }
  }

  it should "create a batch query to counters in several tables while alternating between += and -=" in {
    val id = UUIDs.timeBased()
    val ft = Batch.counter
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.counterTableTest.update.where(_.id eqs id).modify(_.count_entries += 500))

      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries -= 500))
      .add(TestDatabase.secondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries += 500))
      .future()

    val chain = for {
      batched <- ft
      get <- TestDatabase.counterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- TestDatabase.secondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, updated) => {
        info("The first counter select should return the record")
        initial shouldBe defined
        info("and the counter value should match the sum of the +=s")
        initial.value shouldEqual 500

        info("The second counter select should return the record")
        updated shouldBe defined
        info("and the counter value should match the sum of the +=s")
        updated.value shouldEqual 500
      }
    }
  }
}
