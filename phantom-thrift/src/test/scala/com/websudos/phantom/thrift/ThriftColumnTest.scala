/*
 * Copyright 2013-2015 Websudos, Limited.
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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.thrift

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class ThriftColumnTest extends PhantomCassandraTestSuite {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftColumnTable.create.ifNotExists().future().block(2.seconds)
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
        result.get.thriftSet shouldEqual sampleList
      }
    }
  }
}
