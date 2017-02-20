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

import com.outworkers.phantom.tables.{ThriftRecord, ThriftDatabase}
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.finagle._
import com.outworkers.util.testing._
import com.outworkers.util.testing.twitter._
import org.scalatest.FlatSpec

class ThriftMapColumnTest extends FlatSpec with ThriftTestSuite {

  it should "put an item to a thrift map column" in {
    val sample = gen[ThriftRecord]
    val map = genMap[String, ThriftTest]()
    val toAdd = gen[(String, ThriftTest)]
    val expected = map + toAdd

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).future()
      update <- ThriftDatabase.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftMap put toAdd)
        .future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldBe expected
    }
  }

  it should "put an item to a thrift map column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val map = genMap[String, ThriftTest]()
    val toAdd = gen[(String, ThriftTest)]

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).execute
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap put toAdd).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).get
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldBe (map + toAdd)
    }
  }


  it should "put several items to a thrift map column" in {
    val sample = gen[ThriftRecord]

    val toAdd = genMap[String, ThriftTest]()
    val expected = sample.thriftMap ++ toAdd

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).future
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap putAll toAdd).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldBe expected
    }
  }

  it should "put several items to a thrift map column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val map = Map(gen[String] -> sample)
    val toAdd = Map(gen[String] -> sample2, gen[String] -> sample3)
    val expected = map ++ toAdd

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).execute()
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftMap putAll toAdd).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftMap).where(_.id eqs id).get
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldBe expected
    }
  }
}
