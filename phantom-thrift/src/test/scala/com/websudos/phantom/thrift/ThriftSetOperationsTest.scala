/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.thrift.ThriftTest
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class ThriftSetOperationsTest extends BaseTest {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "thriftsetoperators"

  it should "add an item to a thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet add sample2).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
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
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet addAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
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
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet remove sample3).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
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


  it should "remove several items from thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet removeAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
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
