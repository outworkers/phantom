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
package com.websudos.phantom.reactivestreams.suites.iteratee

import java.util.concurrent.atomic.AtomicInteger
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.reactivestreams.iteratee.Iteratee
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class IterateePerformanceTest extends PhantomSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(2 minutes)

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
    TestDatabase.primitivesJoda.insertSchema()
  }

  it should "get retrieve the correct number of results from the database and collect them using an iterator" in {
    val rows = for (i <- 1 to 1000) yield gen[JodaRow]
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      val statement = TestDatabase.primitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val w = batch.future() map (_ => TestDatabase.primitivesJoda.select.fetchEnumerator)
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

  it should "get correctly retrieve the right number of records using asynchronous iterators" in {

    val rows = for (i <- 1 to 100) yield gen[Primitive]
    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(TestDatabase.primitives.store(row))
    })

    val w = TestDatabase.primitives.truncate.future().flatMap {
      _ => batch.future().map(_ => TestDatabase.primitives.select.fetchEnumerator())
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
