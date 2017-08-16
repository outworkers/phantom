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

import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec
import org.scalatest.time.SpanSugar._

class ThriftSetOperationsTest extends FlatSpec with ThriftTestSuite {

  it should "add an item to a thrift set column" in {

    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = thriftDb.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet add sample2).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldBe Set(sample, sample2)
    }
  }

  it should "add several items a thrift set column" in {

    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = thriftDb.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet addAll Set(sample2, sample3)).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value should contain theSameElementsAs Set(sample, sample2, sample3)
    }
  }

  it should "remove one item from a thrift set column" in {

    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = thriftDb.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftSet remove sample3).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value should contain theSameElementsAs Set(sample, sample2)
    }
  }

  it should "remove several items from thrift set column" in {
    val id = gen[UUID]
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sample3 = gen[ThriftTest]

    val insert = thriftDb.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))

    val q = thriftDb
      .thriftColumnTable.update.where(_.id eqs id)
      .modify(_.thriftSet removeAll Set(sample2, sample3))

    val operation = for {
      insertDone <- insert.future()
      update <- thriftDb
        .thriftColumnTable.update.where(_.id eqs id)
        .modify(_.thriftSet removeAll Set(sample2, sample3))
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftSet).where(_.id eqs id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value should contain theSameElementsAs Set(sample)
    }
  }
}
