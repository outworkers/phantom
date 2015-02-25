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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.testkit._
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class IterateeDropTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  implicit val s: PatienceConfiguration.Timeout = timeout(2 minutes)

  ignore should "take records from the iterator" in {

    val rows = for (i <- 1 to 100) yield gen[Primitive]
    var count = 0
    val batch = Iterator.fill(100) {
      val row = rows(count)
      val st = Primitives.insert
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
        .future()
      count += 1
      st
    }

    val traverse = Future.sequence(batch)
    val w = traverse.map(_ => Primitives.select.fetchEnumerator)

    val m = w flatMap {
      en => en run Iteratee.drop(10)
    }

    m successful {
      res =>
        res.toIndexedSeq shouldBe rows.drop(10)
    }
  }
}
