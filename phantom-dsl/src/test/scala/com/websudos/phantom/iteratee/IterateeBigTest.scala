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

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.{ Await, Future }

import org.scalatest.Matchers
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{ PrimitivesJoda, JodaRow }
import com.websudos.util.testing._


class IterateeBigTest extends BigTest with Matchers {

  implicit val s: PatienceConfiguration.Timeout = timeout(12 minutes)

  it should "get result fine" in {
    PrimitivesJoda.insertSchema()
    val fs = for {
      step <- 1 to 100
      rows = Iterator.fill(10000)(gen[JodaRow])

      batch = rows.foldLeft(new BatchStatement())((b, row) => {
        val statement = PrimitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.int)
          .value(_.timestamp, row.bi)
        b.add(statement)
      })
      w = batch.future()
      f = w map (_ => println(s"step $step has succeed") )
      r = Await.result(f, 200 seconds)
    } yield f map (_ => r)


    val combinedFuture = Future.sequence(fs) map {
      r => PrimitivesJoda.count.one()
    }

    val counter: AtomicLong = new AtomicLong(0)
    val result = combinedFuture flatMap {
       rs => {
         info(s"done, inserted: $rs rows - start parsing")
         PrimitivesJoda.select.setFetchSize(10000).fetchEnumerator run Iteratee.forEach { r=> counter.incrementAndGet() }
       }
    }

    (result flatMap (_ => combinedFuture)) successful {
      r => {
        info(s"done, reading: ${counter.get}")
        counter.get() shouldEqual r
      }
    }
  }
}
