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

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.tables.ThriftDatabase
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec

class ThriftColumnTest extends FlatSpec with ThriftTestSuite {

  it should "allow storing thrift columns" in {
    val id = UUIDs.timeBased()
    val sample = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .future() flatMap {
      _ => ThriftDatabase.thriftColumnTable.select.where(_.id eqs id).one()
    }

    whenReady(insert) { result =>
      result.value.struct shouldEqual sample
    }
  }

  it should "allow storing lists of thrift objects" in {
    val id = UUIDs.timeBased()
    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]
    val sampleList = Set(sample, sample2)

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, sampleList)
      .future() flatMap {
        _ => ThriftDatabase.thriftColumnTable.select.where(_.id eqs id).one()
      }

    whenReady(insert) { result =>
      result.value.struct shouldEqual sample
      result.value.thriftSet shouldEqual sampleList
    }
  }
}
