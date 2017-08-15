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

import com.outworkers.phantom.tables.ThriftRecord
import com.outworkers.phantom.finagle._
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec

class ThriftMapColumnTest extends FlatSpec with ThriftTestSuite with TwitterFutures {

  it should "put an item to a thrift map column" in {
    val sample = gen[ThriftRecord]
    val toAdd = gen[(String, ThriftTest)]
    val expected = sample.thriftMap + toAdd

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftMap put toAdd)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual expected
    }
  }

  it should "put an item to a thrift map column with Twitter Futures" in {
    val sample = gen[ThriftRecord]
    val toAdd = gen[(String, ThriftTest)]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap put toAdd).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftMap + toAdd)
    }
  }


  it should "put several items to a thrift map column" in {
    val sample = gen[ThriftRecord]

    val toAdd = genMap[String, ThriftTest]()
    val expected = sample.thriftMap ++ toAdd

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftMap putAll toAdd).future()
      select <- thriftDb.thriftColumnTable
        .select(_.thriftMap)
        .where(_.id eqs sample.id)
        .one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value.size shouldEqual expected.size
      items.value shouldEqual expected
    }
  }

  it should "put several items to a thrift map column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val toAdd = genMap[String, ThriftTest]()
    val expected = sample.thriftMap ++ toAdd

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap putAll toAdd).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value.size shouldEqual expected.size
      items.value shouldEqual expected
    }
  }
}
