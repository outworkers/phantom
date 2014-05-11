/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.ThriftTest
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class OptionalThriftColumnTest extends BaseTest {

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      ThriftColumnTable.insertSchema()
    }
  }

  val keySpace = "optional_thrift_columns"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "find an item if it was defined" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, Some(sample))
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftColumnTable.select(_.optionalThrift).where(_.id eqs sample.id).one
    } yield select

    operation.successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldBe Some(sample)
      }
    }
  }

  it should "not find an item if was not defined" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, None)
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftColumnTable.select(_.optionalThrift).where(_.id eqs sample.id).one
    } yield select

    operation.successful {
      res => {
        res.isDefined shouldBe true
        res.get.isDefined shouldBe false
      }
    }
  }
}
