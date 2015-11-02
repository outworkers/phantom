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
package com.websudos.phantom.builder.query.db.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class SelectOptionalTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    OptionalPrimitives.insertSchema()
  }

  "Selecting the whole row" should "work fine when optional value defined" in {
    checkRow(gen[OptionalPrimitive])
  }

  it should "work fine when optional value is empty" in {
    checkRow(OptionalPrimitive.none)
  }

  private[this] def checkRow(row: OptionalPrimitive) {
    val rcp = for {
      store <- OptionalPrimitives.store(row).future()
      b <- OptionalPrimitives.select.where(_.pkey eqs row.pkey).one
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
}
