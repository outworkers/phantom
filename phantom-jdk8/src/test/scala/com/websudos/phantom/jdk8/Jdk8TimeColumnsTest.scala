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
package com.websudos.phantom.jdk8

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.jdk8.tables.{Jdk8Row, TestDatabase, _}
import com.websudos.util.testing._

class Jdk8TimeColumnsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    if (session.v4orNewer) {
      TestDatabase.primitivesJdk8.insertSchema()
      TestDatabase.optionalPrimitivesJdk8.insertSchema()
    }
  }

  if (session.v4orNewer) {
    it should "correctly insert and extract java.time columns" in {
      val row = gen[Jdk8Row]

      val chain = for {
        store <- TestDatabase.primitivesJdk8.store(row).future()
        select <- TestDatabase.primitivesJdk8.select.where(_.pkey eqs row.pkey).one()
      } yield select

      chain successful {
        res => res.value shouldEqual row
      }
    }

    it should "correctly insert and extract optional java.time columns" in {
      val row = gen[OptionalJdk8Row]

      val chain = for {
        store <- TestDatabase.optionalPrimitivesJdk8.store(row).future()
        select <- TestDatabase.optionalPrimitivesJdk8.select.where(_.pkey eqs row.pkey).one()
      } yield select

      chain successful {
        res => res.value shouldEqual row
      }
    }
  }
}
