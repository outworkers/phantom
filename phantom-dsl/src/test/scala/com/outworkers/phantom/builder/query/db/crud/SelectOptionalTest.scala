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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class SelectOptionalTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.optionalPrimitives.insertSchema()
    if(session.v4orNewer) {
      TestDatabase.optionalPrimitivesCassandra22.insertSchema()
    }
  }

  "Selecting the whole row" should "work fine when optional value defined" in {
    checkRow(gen[OptionalPrimitive])
  }

  it should "work fine when optional value is empty" in {
    checkRow(OptionalPrimitive.empty)
  }


  if (session.v4orNewer) {
    "Selecting the whole row" should "work fine when cassandra 2.2 optional value defined" in {
      checkRow(gen[OptionalPrimitiveCassandra22])
    }

    it should "work fine when optional cassandra 2.2 value is empty" in {
      checkRow(OptionalPrimitiveCassandra22.none)
    }
  }

  private[this] def checkRow(row: OptionalPrimitive): Unit = {
    val rcp = for {
      store <- TestDatabase.optionalPrimitives.store(row).future()
      b <- TestDatabase.optionalPrimitives.select.where(_.pkey eqs row.pkey).one
    } yield b

    rcp successful {
      r => {
        r shouldBe defined
        r.value.bDecimal shouldEqual row.bDecimal
        r.value.bi shouldEqual row.bi
        r.value.boolean shouldEqual row.boolean
        r.value.date shouldEqual row.date
        r.value.double shouldEqual row.double
        r.value.float shouldEqual row.float
        r.value.inet shouldEqual row.inet
        r.value.int shouldEqual row.int
        r.value.long shouldEqual row.long
        r.value.pkey shouldEqual row.pkey
        r.value.string shouldEqual row.string
        r.value.timeuuid shouldEqual row.timeuuid
        r.value.uuid shouldEqual row.uuid
      }
    }
  }

  private[this] def checkRow(row: OptionalPrimitiveCassandra22): Unit = {
    val rcp = for {
      store <- TestDatabase.optionalPrimitivesCassandra22.store(row).future()
      b <- TestDatabase.optionalPrimitivesCassandra22.select.where(_.pkey eqs row.pkey).one
    } yield b

    rcp successful {
      r => {
        r shouldBe defined
        r.value.short shouldEqual row.short
        r.value.byte shouldEqual row.byte
      }
    }
  }
}
