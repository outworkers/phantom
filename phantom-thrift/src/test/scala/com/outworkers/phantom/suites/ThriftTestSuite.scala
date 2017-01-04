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

import java.util.UUID

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.{Output, ThriftDatabase}
import com.outworkers.util.testing._

trait ThriftTestSuite extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftDatabase.create()
  }

  type ThriftTest = com.outworkers.phantom.thrift.ThriftTest
  val ThriftTest = com.outworkers.phantom.thrift.ThriftTest

  implicit object OutputSample extends Sample[Output] {
    def sample: Output = {
      Output(
        id = gen[UUID],
        name = gen[String],
        struct = gen[ThriftTest],
        thriftSet = genList[ThriftTest]().toSet[ThriftTest],
        thriftList = genList[ThriftTest](),
        thriftMap = genList[ThriftTest]().map {
          item => (item.toString, item)
        }.toMap,
        optThrift = genOpt[ThriftTest]
      )
    }
  }

  implicit object ThriftTestSample extends Sample[ThriftTest] {
    def sample: ThriftTest = ThriftTest(
      gen[Int],
      gen[String],
      test = false
    )
  }
}
