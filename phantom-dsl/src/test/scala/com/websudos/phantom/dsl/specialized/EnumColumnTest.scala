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
package com.websudos.phantom.dsl.specialized

import com.datastax.driver.core.utils.UUIDs
import com.twitter.conversions.time._
import com.twitter.util.Await
import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraTestSuite
import com.websudos.phantom.tables.{EnumRecord, EnumTable, Records}
import com.websudos.util.testing._

class EnumColumnTest extends PhantomCassandraTestSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(EnumTable.create.execute(), 2.seconds)
  }

  it should "store a simple record and parse an Enumeration value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, None)


    val chain = for {
      insert <- EnumTable.insert.value(_.id, sample.name).value(_.enum, sample.enum).value(_.optEnum, sample.optEnum).execute()
      get <- EnumTable.select.where(_.id eqs sample.name).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.enum shouldEqual sample.enum
        res.get.optEnum.isDefined shouldEqual false
        res.get.optEnum shouldEqual None
      }
    }
  }

  it should "store a simple record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, Some(Records.TypeTwo))


    val chain = for {
      insert <- EnumTable.insert.value(_.id, sample.name).value(_.enum, sample.enum).value(_.optEnum, sample.optEnum).execute()
      get <- EnumTable.select.where(_.id eqs sample.name).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.enum shouldEqual sample.enum
        res.get.optEnum.isDefined shouldEqual true
        res.get.optEnum shouldEqual sample.optEnum
      }
    }
  }
}
