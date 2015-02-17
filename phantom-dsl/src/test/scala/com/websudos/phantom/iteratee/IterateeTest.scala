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
package com.websudos.phantom.iteratee

import java.util.concurrent.atomic.AtomicInteger
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class IterateeTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(2 minutes)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
    PrimitivesJoda.insertSchema()
  }

  ignore should "get result fine" in {
    val rows = for (i <- 1 to 1000) yield gen[JodaRow]
    val batch = rows.foldLeft(BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val w = batch.future() map (_ => PrimitivesJoda.select.setFetchSize(100).fetchEnumerator)
    w successful {
      en => {
        val result = en run Iteratee.collect()
        result successful {
          seqR =>
            for (row <- seqR)
              rows.contains(row) shouldEqual true
            assert(seqR.size === rows.size)
        }
      }
    }
  }

  it should "get mapResult fine" in {

    val rows = for (i <- 1 to 2000) yield gen[Primitive]
    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
      val statement = Primitives.insert
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
      b.add(statement)
    })

    val w = Primitives.truncate.future().flatMap {
      _ => batch.future().map(_ => Primitives.select.fetchEnumerator())
    }

    val counter: AtomicInteger = new AtomicInteger(0)
    val m = w flatMap {
      en => en run Iteratee.forEach(x => {
        counter.incrementAndGet(); assert(rows.contains(x))
      })
    }

    m successful {
      _ =>
        assert(counter.intValue() === rows.size)
    }
  }
}
