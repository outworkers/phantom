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
import com.websudos.phantom.tables.{TimeUUIDRecord, Recipe}
import com.websudos.phantom.dsl._
import com.outworkers.util.testing._
import com.twitter.conversions.time._

class SelectFunctionsTesting extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.insertSchema()
    database.timeuuidTable.insertSchema()
  }

  it should "retrieve the writetime of a field from Cassandra" in {
    val record = gen[Recipe]


    val chain = for {
      store <- database.recipes.store(record).future()
      timestamp <- database.recipes.select.function(t => writetime(t.description))
        .where(_.url eqs record.url).one()
    } yield timestamp

    whenReady(chain) {
      res => {
        res shouldBe defined
        shouldNotThrow {
          info(res.value.toString)
          new DateTime(res.value.microseconds.inMillis)
        }
      }
    }
  }

  it should "retrieve the dateOf of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord]

    val chain = for {
      store <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => dateOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) {
      res => {
        res shouldBe defined
        shouldNotThrow {
          info(res.value.toString)
        }
      }
    }
  }

  it should "retrieve the unixTimestamp of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord]

    val chain = for {
      store <- database.timeuuidTable.store(record).future()
      timestamp <- database.timeuuidTable.select.function(t => unixTimestampOf(t.id)).where(_.user eqs record.user)
        .and(_.id eqs record.id).one()
    } yield timestamp

    whenReady(chain) {
      res => {
        res shouldBe defined
        shouldNotThrow {
          info(res.value.toString)
        }
      }
    }
  }

  it should "retrieve the TTL of a field from Cassandra" in {
    val record = gen[TimeUUIDRecord]
    val timeToLive = 20

    val chain = for {
      store <- database.timeuuidTable.store(record).ttl(timeToLive).future()
      timestamp <- database.timeuuidTable.select.function(t => ttl(t.name))
        .where(_.user eqs record.user)
        .and(_.id eqs record.id)
        .one()
    } yield timestamp

    whenReady(chain) {
      res => {
        res shouldBe defined
        shouldNotThrow {
          info(res.value.toString)
          res.value.value shouldEqual timeToLive
        }
      }
    }
  }
}
