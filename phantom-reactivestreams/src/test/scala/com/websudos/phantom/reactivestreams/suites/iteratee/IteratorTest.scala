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
package com.websudos.phantom.reactivestreams.suites.iteratee

import java.util.concurrent.atomic.AtomicInteger

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Primitive, TestDatabase}
import com.websudos.util.testing._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class IteratorTest extends BigTest with ScalaFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.insertSchema()
  }

  it should "correctly retrieve the right number of records using scala iterator" in {
    val rows = for (i <- 1 to 100) yield gen[Primitive]

    val setUpFuture = TestDatabase.primitives.truncate.future().flatMap {
      _ => Future.sequence(rows.map(row => TestDatabase.primitives.store(row).future()))
    }

    val counter: AtomicInteger = new AtomicInteger(0)

    val iterationResultFuture = setUpFuture.flatMap(_ => TestDatabase.primitives.select.iterator()).map {
      _.foreach(x => {
        counter.incrementAndGet()
        assert(rows.contains(x))
      })
    }

    whenReady(iterationResultFuture) {
      _ => assert(counter.intValue() === rows.size)
    }
  }
}
