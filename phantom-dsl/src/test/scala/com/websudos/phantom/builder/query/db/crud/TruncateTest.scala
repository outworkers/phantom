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

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class TruncateTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.articles.insertSchema()
  }

  it should "truncate all records in a table" in {
    val article1 = gen[Article]
    val article2 = gen[Article]
    val article3 = gen[Article]
    val article4 = gen[Article]

    val result = for {
      truncateBefore <- TestDatabase.articles.truncate.future()
      i1 <- TestDatabase.articles.store(article1).future()
      i2 <- TestDatabase.articles.store(article2).future()
      i3 <- TestDatabase.articles.store(article3).future()
      i4 <- TestDatabase.articles.store(article4).future()

      records <- TestDatabase.articles.select.fetch
      truncate <- TestDatabase.articles.truncate.future()
      records1 <- TestDatabase.articles.select.fetch
    } yield (records, records1)


    result successful {
      case (init, updated) => {
        init should have size 4
        info (s"inserted exactly ${init.size} records")

        updated should have size 0
        info (s"got exactly ${updated.size} records")
      }
    }
  }

  it should "truncate all records in a table with Twitter Futures" in {
    val article1 = gen[Article]
    val article2 = gen[Article]
    val article3 = gen[Article]
    val article4 = gen[Article]

    val result = for {
      truncateBefore <- TestDatabase.articles.truncate.execute()
      i1 <- TestDatabase.articles.store(article1).execute()
      i2 <- TestDatabase.articles.store(article2).execute()
      i3 <- TestDatabase.articles.store(article3).execute()
      i4 <- TestDatabase.articles.store(article4).execute()

      records <- TestDatabase.articles.select.collect()
      truncate <- TestDatabase.articles.truncate.execute()
      records1 <- TestDatabase.articles.select.collect()
    } yield (records, records1)


    result successful {
      case (init, updated) => {
        init should have size 4
        info (s"inserted exactly ${init.size} records")

        updated should have size 0
        info (s"got exactly ${updated.size} records")
      }
    }
  }
}
