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

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{ TestDatabase, Recipe }
import com.websudos.util.testing._

class InOperatorTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.recipes.insertSchema()
  }

  it should "find a record with a in operator if the record exists" in {
    val recipe = gen[Recipe]

    val chain = for {
      done <- TestDatabase.recipes.store(recipe).future()
      select <- TestDatabase.recipes.select.where(_.url in List(recipe.url, gen[EmailAddress].value)).one()
    } yield select

    chain.successful {
      res => {
        res.value.url shouldEqual recipe.url
      }
    }
  }

  it should "not find a record with a in operator if the record doesn't exists" in {
    val recipe = gen[Recipe]

    val chain = for {
      done <- TestDatabase.recipes.store(recipe).future()
      select <- TestDatabase.recipes.select.where(_.url in List(gen[EmailAddress].value)).one()
    } yield select

    chain.successful {
      res => {
        res shouldBe empty
      }
    }
  }

}
