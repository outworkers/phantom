/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.thrift.tests.binary.suites

import com.outworkers.phantom.finagle._
import com.outworkers.phantom.thrift.binary._
import com.outworkers.phantom.thrift.tests.ThriftRecord
import com.outworkers.phantom.thrift.tests.binary.BinarySuite
import com.outworkers.phantom.thrift.tests.compact.CompactSuite
import com.outworkers.util.samplers._

class ThriftIndexTableTest extends BinarySuite {

  it should "allow storing a thrift class inside a table indexed by a thrift struct" in {
    val sample = gen[ThriftRecord]

    val chain = for {
      _ <- thriftDb.thriftIndexedTable.store(sample).future()
      get <- thriftDb.thriftIndexedTable.select.where(_.ref eqs sample.struct).one()
    } yield get

    whenReady(chain) { res =>
      res.value.id shouldEqual sample.id
      res.value.name shouldEqual sample.name
      res.value.struct shouldEqual sample.struct
      res.value.optThrift shouldEqual sample.optThrift
      res.value.thriftList shouldEqual sample.thriftList
      res.value.thriftMap shouldEqual sample.thriftMap
      res.value.thriftSet shouldEqual sample.thriftSet

      res.value shouldEqual sample
    }
  }

  it should "allow storing a thrift class inside a table indexed by a thrift struct with Twitter futures" in {
    val sample = gen[ThriftRecord]

    val chain = for {
      store <- thriftDb.thriftIndexedTable.store(sample).future()
      get <- thriftDb.thriftIndexedTable.select.where(_.ref eqs sample.struct).one()
    } yield get

    whenReady(chain) { res =>
      res.value.id shouldEqual sample.id
      res.value.name shouldEqual sample.name
      res.value.struct shouldEqual sample.struct
      res.value.optThrift shouldEqual sample.optThrift
      res.value.thriftList shouldEqual sample.thriftList
      res.value.thriftMap shouldEqual sample.thriftMap
      res.value.thriftSet shouldEqual sample.thriftSet

      res.value shouldEqual sample
    }
  }
}
