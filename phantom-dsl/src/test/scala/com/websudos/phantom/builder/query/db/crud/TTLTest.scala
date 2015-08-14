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

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Primitive, Primitives}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._
import org.scalatest.concurrent.{Eventually, PatienceConfiguration}
import org.scalatest.time.SpanSugar._
import org.scalatest.time.{Millis, Seconds, Span}

class TTLTest extends PhantomCassandraTestSuite with Eventually {

  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  override implicit val patienceConfig = PatienceConfig(Span(7, Seconds), Span(200, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  it should "expire inserted records after 2 seconds" in {
    val row = gen[Primitive]

    val chain = for {
      store <- Primitives.store(row).ttl(2).future()
      get <- Primitives.select.where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row

        eventually {
          val record = Primitives.select.where(_.pkey eqs row.pkey).one().block(3.seconds)
          record.isDefined shouldEqual false
        }
      }
    }
  }

  it should "expire inserted records after 2 seconds with Twitter Futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- Primitives.store(row).ttl(2).execute()
      get <- Primitives.select.where(_.pkey eqs row.pkey).get()
    } yield get

    chain.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get shouldEqual row

        eventually {
          val record = Primitives.select.where(_.pkey eqs row.pkey).one().block(3.seconds)
          record.isDefined shouldEqual false
        }
      }
    }
  }
}
