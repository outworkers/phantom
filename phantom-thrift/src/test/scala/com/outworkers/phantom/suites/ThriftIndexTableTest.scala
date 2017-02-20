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

import com.outworkers.phantom.tables.{ThriftRecord, ThriftDatabase}
import com.outworkers.util.testing._
import com.twitter.scrooge.CompactThriftSerializer
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.finagle._
import com.outworkers.phantom.thrift.ThriftPrimitive
import org.scalatest.FlatSpec

class ThriftIndexTableTest extends FlatSpec with ThriftTestSuite {

  val ThriftIndexedTable = ThriftDatabase.thriftIndexedTable

  implicit object SamplePrimitive extends ThriftPrimitive[ThriftTest] {
    val serializer = CompactThriftSerializer(ThriftTest)
  }

  it should "allow storing a thrift class inside a table indexed by a thrift struct" in {
    val sample = gen[ThriftRecord]

    val chain = for {
      store <- ThriftIndexedTable.store(sample).future()
      get <- ThriftIndexedTable.select.where(_.ref eqs sample.struct).one()
    } yield get

    chain.successful {
      res => {
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

  it should "allow storing a thrift class inside a table indexed by a thrift struct with Twitter futures" in {
    val sample = gen[ThriftRecord]

    val chain = for {
      store <- ThriftIndexedTable.store(sample).execute()
      get <- ThriftIndexedTable.select.where(_.ref eqs sample.struct).get()
    } yield get

    chain.successful {
      res => {
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
}
