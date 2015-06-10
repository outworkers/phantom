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
package com.websudos.phantom.builder.query.db.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.phantom.tables.{StaticCollectionRecord, StaticCollectionTableTest, StaticTableTest}
import com.websudos.util.testing._

class StaticColumnTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    StaticTableTest.insertSchema()
    StaticCollectionTableTest.insertSchema()
  }

  it should "use a static value for a static column" in {

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val id2 = UUIDs.timeBased()
    val chain = for {
      // The first record holds the static value.
      insert <- StaticTableTest.insert.value(_.id, id).value(_.clusteringId, id).value(_.staticTest, static).execute()
      insert2 <- StaticTableTest.insert.value(_.id, id).value(_.clusteringId, id2).execute()
      select <- StaticTableTest.select.where(_.id eqs id).and(_.clusteringId eqs id2).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get._3 shouldEqual static
      }
    }
  }

  it should "update values in all rows" in {

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val static2 = "this_is_updated_static"
    val id2 = UUIDs.timeBased()
    val chain = for {

      // The first insert holds the first static value.
      insert <- StaticTableTest.insert.value(_.id, id).value(_.clusteringId, id).value(_.staticTest, static).execute()

      // The second insert updates the static value
      insert2 <- StaticTableTest.insert.value(_.id, id).value(_.clusteringId, id2).value(_.staticTest, static2).execute()

      // We query for the first record inserted.
      select <- StaticTableTest.select.where(_.id eqs id).and(_.clusteringId eqs id).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        // The first record should hold the updated value.
        res.get._3 shouldEqual static2
      }
    }
  }

  it should "append a value to a static list and share the update among records" in {
    val id = gen[UUID]

    val sample = gen[StaticCollectionRecord].copy(id = id)
    val sample2 = gen[StaticCollectionRecord].copy(id = id, list = sample.list)

    val chain = for {
      store1 <- StaticCollectionTableTest.store(sample).future()
      store2 <- StaticCollectionTableTest.store(sample2).future()
      update <- StaticCollectionTableTest.update.where(_.id eqs id).and(_.clusteringId eqs sample.clustering).modify(_.staticList append "test").future()
      get <- StaticCollectionTableTest.select.where(_.id eqs id).and(_.clusteringId eqs sample.clustering).one()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.list shouldEqual sample.list ::: List("test")
      }
    }
  }

}
