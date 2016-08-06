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
package com.websudos.phantom.builder.query.db.crud

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.outworkers.util.testing._
import net.liftweb.http.js.JsObj
import net.liftweb.json.JsonParser

class SelectJsonTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "A JSON selection clause" should "select an entire row as JSON" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      b <- TestDatabase.primitives.select.json().where(_.pkey eqs row.pkey).one
    } yield b

    chain successful {
      res => {
        res shouldBe defined
        val parsed = JsonParser.parse(res.value)
        parsed.children.size shouldEqual row.productArity
      }
    }
  }

  "A JSON selection clause" should "8 columns as JSON" in {
    val row = gen[Primitive]
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      get <- TestDatabase.primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int)
        .json()
        .where(_.pkey eqs row.pkey).one()
    } yield get

    chain successful {
      res => {
        res shouldBe defined
        val parsed = JsonParser.parse(res.value)
        parsed.children.size shouldEqual expected.productArity
      }
    }
  }
}