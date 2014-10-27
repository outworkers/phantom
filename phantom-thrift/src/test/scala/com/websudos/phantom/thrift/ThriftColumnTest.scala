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
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class ThriftColumnTest extends PhantomCassandraTestSuite {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftColumnTable.insertSchema
  }

  it should "allow storing thrift columns" in {
    val id = UUIDs.timeBased()
    val sample = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .future() flatMap {
      _ => ThriftColumnTable.select.where(_.id eqs id).one()
    }

    insert.successful {
      result => {
        result.isEmpty shouldEqual false
        result.get.struct shouldEqual sample
      }
    }
  }

  it should "allow storing lists of thrift objects" in {
    val id = UUIDs.timeBased()
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sampleList = Set(sample, sample2)

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, sampleList)
      .future() flatMap {
      _ => ThriftColumnTable.select.where(_.id eqs id).one()
    }

    insert.successful {
      result => {
        result.isEmpty shouldEqual false
        result.get.struct shouldEqual sample
        result.get.list shouldEqual sampleList
      }
    }
  }
}
