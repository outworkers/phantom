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
package com.websudos.phantom.builder.query.db.select

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._


class PartialSelectTest extends PhantomCassandraTestSuite {

  override implicit val patience: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "Partially selecting 2 fields" should "correctly select the fields" in {
    val row = gen[Primitive]

    val chain = for {
      truncate <- TestDatabase.primitives.truncate.future()
      insertDone <- TestDatabase.primitives.store(row).future()
      listSelect <- TestDatabase.primitives.select(_.pkey).fetch
      oneSelect <- TestDatabase.primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
    } yield (listSelect, oneSelect)

    chain successful {
      result => {
        result._1 shouldEqual List(row.pkey)
        result._2.value shouldEqual Tuple2(row.long, row.boolean)
      }
    }
  }

  "Partially selecting 2 fields" should "work fine with Twitter Futures" in {
    val row = gen[Primitive]

    val chain = for {
      truncate <- TestDatabase.primitives.truncate.execute()
      insertDone <- TestDatabase.primitives.store(row).execute()
      listSelect <- TestDatabase.primitives.select(_.pkey).collect
      oneSelect <- TestDatabase.primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).get
    } yield (listSelect, oneSelect)

    chain successful {
      result => {
        result._1.toList shouldEqual List(row.pkey)
        result._2 shouldEqual Some(Tuple2(row.long, row.boolean))
      }
    }
  }
}
