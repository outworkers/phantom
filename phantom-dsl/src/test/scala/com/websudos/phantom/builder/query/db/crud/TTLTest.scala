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

import com.twitter.util.Duration
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Primitive, Primitives}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class TTLTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  it should "expire inserted records after 2 seconds" in {
    val row = gen[Primitive]
    val test = Primitives.insert
        .value(_.pkey, row.pkey)
        .value(_.long, row.long)
        .value(_.boolean, row.boolean)
        .value(_.bDecimal, row.bDecimal)
        .value(_.double, row.double)
        .value(_.float, row.float)
        .value(_.inet, row.inet)
        .value(_.int, row.int)
        .value(_.date, row.date)
        .value(_.uuid, row.uuid)
        .value(_.bi, row.bi)
        .ttl(2)
        .future() flatMap {
          _ =>  Primitives.select.where(_.pkey eqs row.pkey).one()
        }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row
        Thread.sleep(Duration.fromSeconds(3).inMillis)
        val test2 = Primitives.select.where(_.pkey eqs row.pkey).one()
        test2 successful {
          expired => {
            expired.isEmpty shouldEqual true
          }
        }
      }
    }
  }

  it should "expire inserted records after 2 seconds with Twitter Futures" in {
    val row = gen[Primitive]
    val test = Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
      .ttl(2)
      .execute() flatMap {
      _ =>  Primitives.select.where(_.pkey eqs row.pkey).get()
    }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row
        Thread.sleep(Duration.fromSeconds(3).inMillis)
        val test2 = Primitives.select.where(_.pkey eqs row.pkey).get
        test2 successful {
          expired => {
            expired.isEmpty shouldEqual true
          }
        }
      }
    }
  }
}
