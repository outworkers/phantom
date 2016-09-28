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
package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._

import scala.concurrent.Await

import com.outworkers.util.testing._

class EnumColumnTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(database.enumTable.create.ifNotExists().future(), defaultScalaTimeout)
    Await.result(database.namedEnumTable.create.ifNotExists().future(), defaultScalaTimeout)
    Await.result(database.indexedEnumTable.create.ifNotExists().future(), defaultScalaTimeout)
  }

  it should "store a simple record and parse an Enumeration value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, None)

    val chain = for {
      insert <- database.enumTable.store(sample).future()
      get <- database.enumTable.select.where(_.id eqs sample.name).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value.enum shouldEqual sample.enum
        res.value.optEnum shouldBe empty
      }
    }
  }

  it should "store a simple record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, Some(Records.TypeTwo))

    val chain = for {
      insert <- database.enumTable.store(sample).future()
      get <- database.enumTable.select.where(_.id eqs sample.name).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value.enum shouldEqual sample.enum
        res.value.optEnum shouldBe defined
        res.value.optEnum.value shouldBe Records.TypeTwo
      }
    }
  }

  it should "store a named record and parse an Enumeration value back from the stored value" in {
    val sample = NamedEnumRecord(UUIDs.timeBased().toString, NamedRecords.One, None)

    val chain = for {
      insert <- database.namedEnumTable.store(sample).future()
      get <- database.namedEnumTable.select.where(_.id eqs sample.name).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value.enum shouldEqual sample.enum
        res.value.optEnum shouldBe empty
      }
    }
  }

  it should "store a named record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = NamedEnumRecord(UUIDs.timeBased().toString, NamedRecords.One, Some(NamedRecords.Two))

    val chain = for {
      insert <- database.namedEnumTable.store(sample).future()
      get <- database.namedEnumTable.select.where(_.id eqs sample.name).one()
    } yield get

    whenReady(chain) {
      res => {
        res.value.enum shouldEqual sample.enum
        res.value.optEnum shouldBe defined
        res.value.optEnum shouldEqual sample.optEnum
      }
    }
  }

  it should "store and retrieve an indexed enumeration record" in {
    val sample = NamedPartitionRecord(
      NamedRecords.One,
      UUIDs.random()
    )

    val chain = for {
      store <- database.indexedEnumTable.store(sample).future()
      get <- database.indexedEnumTable.select.where(_.enum eqs NamedRecords.One).one()
    } yield get

    whenReady(chain) { res =>
      res.value shouldEqual sample
    }
  }

}


