/*
 * Copyright 2013-2016 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom.suites

import com.outworkers.phantom.tables.{Output, ThriftDatabase}
import com.outworkers.phantom.thrift.columns.ThriftPrimitive
import com.outworkers.phantom.thrift.suites.ThriftTest
import com.outworkers.util.testing._
import com.twitter.scrooge.CompactThriftSerializer
import com.outworkers.phantom.dsl._
import com.websudos.phantom.finagle._
import org.scalatest.FlatSpec

class ThriftIndexTableTest extends FlatSpec with ThriftTestSuite {

  val ThriftIndexedTable = ThriftDatabase.thriftIndexedTable

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftIndexedTable.insertSchema()
  }

  implicit object SamplePrimitive extends ThriftPrimitive[ThriftTest] {
    val serializer = CompactThriftSerializer(ThriftTest)
  }

  it should "allow storing a thrift class inside a table indexed by a thrift struct" in {
    val sample = gen[Output]

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
    val sample = gen[Output]

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
