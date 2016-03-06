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
import com.websudos.phantom.tables.ThriftDatabase
import com.websudos.util.testing._
import org.scalatest.FlatSpec
import org.scalatest.time.SpanSugar._

class ThriftMapColumnTest extends FlatSpec with ThriftTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftDatabase.thriftColumnTable.create.ifNotExists().future().block(5.seconds)
  }

  it should "put an item to a thrift map column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val map = Map(gen[String] -> sample)
    val toAdd = gen[String] -> sample2
    val expected = map + toAdd


    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)

      .future()


    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftMap put toAdd).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldBe expected
      }
    }
  }

  it should "put an item to a thrift map column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val map = Map(gen[String] -> sample)
    val toAdd = gen[String] -> sample2
    val expected = map + toAdd


    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)
      .execute()


    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftMap put toAdd).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldBe expected
      }
    }
  }


  it should "put several items to a thrift map column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val map = Map(gen[String] -> sample)
    val toAdd = Map(gen[String] -> sample2, gen[String] -> sample3)
    val expected = map ++ toAdd


    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftMap putAll toAdd).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldBe expected
      }
    }
  }

  it should "put several items to a thrift map column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val map = Map(gen[String] -> sample)
    val toAdd = Map(gen[String] -> sample2, gen[String] -> sample3)
    val expected = map ++ toAdd


    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftMap putAll toAdd).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldBe expected
      }
    }
  }
}
