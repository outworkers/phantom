/*
 * Copyright 2013-2017 Outworkers, Limited.
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
package com.outworkers.phantom.base

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{Article, TestDatabase}
import com.outworkers.util.testing._

import scala.concurrent.{Future, blocking}

class SkipRecordsByToken extends PhantomSuite {

  val Articles = TestDatabase.articles

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Articles.insertSchema()
    }
  }

  it should "allow skipping records using gtToken" in {
    val article1 = gen[Article]
    val article2 = gen[Article]
    val article3 = gen[Article]
    val article4 = gen[Article]

    val result = for {
      truncate <- Articles.truncate.future()
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.orderId, article1.orderId)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.orderId, article2.orderId)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.orderId, article3.orderId)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.orderId, article4.orderId)
        .future()
      one <- Articles.select.one
      next <- Articles.select.where(_.id gtToken one.value.id).fetch
    } yield next

    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 3
      }
    }
  }

  it should "allow skipping records using eqsToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Future.sequence(articles.map(Articles.store(_).future()))

      next <- Articles.select.where(_.id eqsToken articles.headOption.value.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 1
      }
    }
  }

  ignore should "allow skipping records using gteToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Future.sequence(articles.map(Articles.store(_).future()))

      next <- Articles.select.where(_.id gteToken articles.headOption.value.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual 3
      }
    }
  }

  ignore should "allow skipping records using ltToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Future.sequence(articles.map(Articles.store(_).future()))

      next <- Articles.select.where(_.id ltToken articles.lastOption.value.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual (articles.size - 1)
      }
    }
  }

  ignore should "allow skipping records using lteToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Future.sequence(articles.map(Articles.store(_).future()))
      next <- Articles.select.where(_.id lteToken articles.lastOption.value.id).fetch
    } yield next


    result successful {
      r => {
        info (s"got exactly ${r.size} records")
        r.size shouldEqual (articles.size - 1)
      }
    }
  }

}
