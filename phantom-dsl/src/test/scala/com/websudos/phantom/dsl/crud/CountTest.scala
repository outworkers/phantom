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
package com.websudos.phantom.dsl.crud


import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl.Batch
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class CountTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    PrimitivesJoda.insertSchema()
  }


  it should "retrieve a count of 0 if the table has been truncated" in {

    val chain = for {
      truncate <- PrimitivesJoda.truncate.future()
      count <- PrimitivesJoda.select.count.fetch()
    } yield count

    chain successful {
      res => {
        res.isEmpty shouldEqual false
        res.head shouldEqual 0L
      }
    }
  }

  it should "correctly retrieve a count of 1000" in {
    val limit = 1000

    val rows = genList[JodaRow](limit)

    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val chain = for {
      truncate <- PrimitivesJoda.truncate.future()
      batch <- batch.future()
      count <- PrimitivesJoda.select.count.one()
    } yield count

    chain successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldEqual 999L
      }
    }
  }

  it should "correctly retrieve a count of 1000 with Twitter futures" in {
    val limit = 1000

    val rows = genList[JodaRow](limit)

    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val chain = for {
      truncate <- PrimitivesJoda.truncate.execute()
      batch <- batch.execute()
      count <- PrimitivesJoda.select.count.get()
    } yield count

    chain successful {
      res => {
        res.isDefined shouldBe true
        res.get shouldEqual 999L
      }
    }
  }
}
