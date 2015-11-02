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
package com.websudos.phantom.thrift.suites

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.testkit._
import com.websudos.util.testing._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class ThriftSetOperationsTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftColumnTable.create.ifNotExists().future().block(5.seconds)
  }

  it should "add an item to a thrift set column" in {

    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet add sample2).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2)
      }
    }
  }

  it should "add several items a thrift set column" in {

    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet addAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2, sample3)
      }
    }
  }

  it should "remove one item from a thrift set column" in {

    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet remove sample3).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2)
      }
    }
  }


  it should "remove several items from thrift set column" in {
    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet removeAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample)
      }
    }
  }
}
