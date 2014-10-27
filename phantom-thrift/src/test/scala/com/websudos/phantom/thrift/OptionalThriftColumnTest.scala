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
package com.websudos.phantom.thrift

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.datastax.driver.core.utils.UUIDs
import com.websudos.util.testing._
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.testing._

class OptionalThriftColumnTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftColumnTable.insertSchema()
  }

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "find an item if it was defined" in {

    val id = UUIDs.timeBased()

    val sample = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, Some(sample))
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftColumnTable.select(_.optionalThrift).where(_.id eqs id).one
    } yield select

    operation.successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldBe Some(sample)
      }
    }
  }

  it should "not find an item if was not defined" in {
    val id = UUIDs.timeBased()

    val sample = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, None)
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftColumnTable.select(_.optionalThrift).where(_.id eqs id).one
    } yield select

    operation.successful {
      res => {
        res.isDefined shouldBe true
        res.get.isDefined shouldBe false
      }
    }
  }
}
