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
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.ThriftDatabase
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec
import org.scalatest.time.SpanSugar._

class OptionalThriftColumnTest extends FlatSpec with ThriftTestSuite with TwitterFutures {

  it should "find an item if it was defined" in {

    val id = UUIDs.timeBased()

    val sample = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, Some(sample))
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftDatabase.thriftColumnTable.select(_.optionalThrift).where(_.id eqs id).one
    } yield select

    whenReady(operation) { res =>
      res.value shouldBe Some(sample)
    }
  }

  it should "not find an item if was not defined" in {
    val id = UUIDs.timeBased()

    val sample = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.optionalThrift, None)
      .future()

    val operation = for {
      insertDone <- insert
      select <- ThriftDatabase.thriftColumnTable.select(_.optionalThrift).where(_.id eqs id).one
    } yield select

    whenReady(operation) { res =>
      res.value.isDefined shouldBe false
    }
  }
}
