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

import scala.concurrent.{Await, blocking}
import scala.concurrent.duration._
import com.datastax.driver.core.exceptions.InvalidQueryException
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

import scala.util.Try

class SecondaryIndexTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()

    blocking {
      Try {
        session.execute(s"DROP TABLE ${keySpace.name}.${SecondaryIndexTable.tableName}")
      }
    }

    Await.ready(SecondaryIndexTable.create.ifNotExists().future(), 4.seconds)
  }

  it should "allow fetching a record by its secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).future()
      select <- SecondaryIndexTable.select.where(_.id eqs sample.primary).one
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).allowFiltering().one()
    } yield (select, select2)

    chain.successful {
      res => {
        val primary = res._1
        val secondary = res._2

        info("Querying by primary key should return the record")
        primary.isDefined shouldBe true
        primary.get shouldEqual sample

        info("Querying by the secondary index key should also return the record")
        secondary.isDefined shouldEqual true
        secondary.get shouldEqual sample
      }
    }
  }

  it should "allow fetching a record by its secondary index with Twitter Futures" in {
    val sample = gen[SecondaryIndexRecord]

    val chain = for {
      insert <- SecondaryIndexTable.store(sample).execute()
      select <- SecondaryIndexTable.select.where(_.id eqs sample.primary).get
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).allowFiltering().get()
    } yield (select, select2)

    chain.successful {
      res => {
        val primary = res._1
        val secondary = res._2

        info("Querying by primary key should return the record")
        primary.isDefined shouldBe true
        primary.get shouldEqual sample

        info("Querying by the secondary index key should also return the record")
        secondary.isDefined shouldEqual true
        secondary.get shouldEqual sample
      }
    }
  }

  it should "allow updating the value of a secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val updated = gen[UUID]

    val chain = for {
      insert <- SecondaryIndexTable.store(sample).future()
      selected <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).allowFiltering().one()
      select <- SecondaryIndexTable.update.where(_.id eqs sample.primary).modify(_.secondary setTo updated).future()
      updated <- SecondaryIndexTable.select.where(_.secondary eqs updated).allowFiltering().one()
    } yield (selected, updated)

    chain.successful {
      res => {
        val primary = res._1
        val secondary = res._2

        info("Querying by primary key should return the record")
        primary.isDefined shouldBe true
        primary.get shouldEqual sample

        info("Querying by the secondary index key should also return the record")
        secondary.isDefined shouldEqual true
        secondary.get shouldEqual sample.copy(secondary = updated)
      }
    }
  }

  it should "not throw an error if filtering is not enabled when querying by secondary keys" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield select2

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual sample
      }
    }
  }

  it should "not throw an error if filtering is not enabled when querying by secondary keys with Twitter Futures" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield select2

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual sample
      }
    }
  }

  it should "throw an error when updating a record by its secondary key" in {
    val sample = gen[SecondaryIndexRecord]
    val updatedName = gen[String]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      update <- SecondaryIndexTable.update.where(_.secondary eqs sample.secondary).modify(_.name setTo updatedName).future()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

  it should "throw an error when updating a record by its secondary key with Twitter Futures" in {
    val sample = gen[SecondaryIndexRecord]
    val updatedName = gen[String]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
      update <- SecondaryIndexTable.update.where(_.secondary eqs sample.secondary).modify(_.name setTo updatedName).execute()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield (select2, select3)


    chain.failing[InvalidQueryException]
  }

  it should "throw an error when deleting a record by its secondary index" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      delete <- SecondaryIndexTable.delete.where(_.secondary eqs sample.secondary).future()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

  it should "throw an error when deleting a record by its secondary index with Twitter Futures" in {
    val sample = gen[SecondaryIndexRecord]
    val chain = for {
      insert <- SecondaryIndexTable.store(sample).execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
      delete <- SecondaryIndexTable.delete.where(_.secondary eqs sample.secondary).execute()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

}
