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
package com.websudos.phantom.builder.query.db.crud


import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl.Batch
import com.websudos.phantom.tables._
import com.websudos.util.testing._

import scala.concurrent.ExecutionContext.Implicits.global

class CountTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitivesJoda.insertSchema()
  }


  it should "retrieve a count of 0 if the table has been truncated" in {

    val chain = for {
      truncate <- TestDatabase.primitivesJoda.truncate.future()
      count <- TestDatabase.primitivesJoda.select.count.one()
    } yield count

    chain successful {
      res => {
        res.value shouldEqual 0L
      }
    }
  }

  it should "correctly retrieve a count of 1000" in {
    val limit = 100

    val rows = genList[JodaRow](limit)

    val batch = rows.foldLeft(Batch.unlogged)((b, row) => {
      b.add(TestDatabase.primitivesJoda.store(row))
    })

    val chain = for {
      truncate <- TestDatabase.primitivesJoda.truncate.future()
      batch <- batch.future()
      count <- TestDatabase.primitivesJoda.select.count.one()
    } yield count

    chain successful {
      res => {
        res.value shouldEqual limit.toLong
      }
    }
  }
}
