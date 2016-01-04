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
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class DeleteTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
  }

  "A delete query" should "delete a row by its single primary key" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).future()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row
        r._2 shouldBe empty
      }
    }
  }

  "A delete query" should "delete a row by its single primary key using Twitter futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).execute()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).execute()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row
        r._2 shouldBe empty
      }
    }
  }

  "A delete query" should "delete a row by its single primary key if a single condition is met" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is row.int).future()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row
        r._2 shouldBe empty
      }
    }
  }

  "A delete query" should "delete a row by its single primary key if a single condition is met with Twitter futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).execute()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is row.int).execute()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row
        r._2 shouldBe empty
      }
    }
  }

  "A delete query" should "not delete a row by its single primary key if a single condition is not met" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is (row.int + 1)).future()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row

        info("The row should not have been deleted as the condition was not met")
        r._2 shouldBe defined
      }
    }
  }

  "A delete query" should "not delete a row by its single primary key if a single condition is not met with Twitter Futures" in {
    val row = gen[Primitive]

    val chain = for {
      store <- TestDatabase.primitives.store(row).execute()
      inserted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get()
      delete <- TestDatabase.primitives.delete.where(_.pkey eqs row.pkey).onlyIf(_.int is (row.int + 1)).execute()
      deleted <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).get
    } yield (inserted, deleted)

    chain successful {
      r => {
        r._1.value shouldEqual row

        info("The row should not have been deleted as the condition was not met")
        r._2 shouldBe defined
      }
    }
  }

}
