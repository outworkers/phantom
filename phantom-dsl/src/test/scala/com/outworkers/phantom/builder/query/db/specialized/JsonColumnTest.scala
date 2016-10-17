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

package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class JsonColumnTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.jsonTable.insertSchema()
  }

  it should "allow storing a JSON record" in {
    val sample = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
    } yield select

    chain.successful {
      res => {
        res.value shouldEqual sample
      }
    }
  }

  it should "allow updating a JSON record" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value.json shouldEqual sample.json
        updated.value.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value shouldEqual sample
        updated.value.jsonList.headOption.value shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a Set of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value shouldEqual sample
        updated.value.jsonSet should contain (sample2.json)
      }
    }
  }
}
