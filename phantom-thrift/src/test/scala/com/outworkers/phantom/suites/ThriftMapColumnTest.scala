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
package com.outworkers.phantom.suites

import com.outworkers.phantom.tables.ThriftDatabase
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.finagle._
import com.outworkers.util.testing._
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
      update <- ThriftDatabase.thriftColumnTable.update
        .where(_.id eqs id).modify(_.thriftMap put toAdd).future()
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
