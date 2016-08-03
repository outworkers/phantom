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
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.outworkers.util.testing._
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.{Assertions, Matchers}

class UpdateTest extends PhantomSuite with Matchers with Assertions with AsyncAssertions {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.primitives.insertSchema()
    TestDatabase.testTable.insertSchema()
    TestDatabase.optionalPrimitives.insertSchema()
  }

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[Primitive]

    val updatedRow = gen[Primitive].copy(pkey = row.pkey)

    val chain = for {
      store <- TestDatabase.primitives.store(row).future()
      a <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
      u <- TestDatabase.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setTo updatedRow.long)
        .and(_.boolean setTo updatedRow.boolean)
        .and(_.bDecimal setTo updatedRow.bDecimal)
        .and(_.double setTo updatedRow.double)
        .and(_.float setTo updatedRow.float)
        .and(_.inet setTo updatedRow.inet)
        .and(_.int setTo updatedRow.int)
        .and(_.date setTo updatedRow.date)
        .and(_.uuid setTo updatedRow.uuid)
        .and(_.bi setTo updatedRow.bi)
        .future()
      a2 <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) {
      case (res1, res2) => {
        res1.value shouldEqual row
        res2.value shouldEqual updatedRow
      }
    }
  }

  it should "successfully store a table with a mixture of collection columns: List, Set, Map" in {

    val row = gen[TestRow].copy(mapIntToInt = Map.empty)

    val updatedRow = row.copy(
      list = List("new"),
      setText = Set("newSet"),
      mapTextToText =  Map("n" -> "newVal"),
      setInt = Set(3, 4, 7),
      mapIntToText = Map (-1 -> "&&&")
    )

    val chain = for {
      store <- TestDatabase.testTable.store(row).future()
      a <- TestDatabase.testTable.select.where(_.key eqs row.key).one
      u <- TestDatabase.testTable.update
        .where(_.key eqs row.key)
        .modify(_.list setTo updatedRow.list)
        .and(_.setText setTo updatedRow.setText)
        .and(_.mapTextToText setTo updatedRow.mapTextToText)
        .and(_.setInt setTo updatedRow.setInt)
        .and(_.mapIntToText setTo updatedRow.mapIntToText).future()
      a2 <- TestDatabase.testTable.select.where(_.key eqs row.key).one
    } yield (a, a2)

    chain successful {
      case (res1, res2) => {
        res1.value shouldEqual row
        res2.value shouldEqual updatedRow
      }
    }
  }

  it should "store an OptionalPrimitive in an optional table if none of the columns specified are null" in {
    val sample = OptionalPrimitive.empty
    val updated = gen[OptionalPrimitive].copy(pkey = sample.pkey)

    val chain = for {
      store <- database.optionalPrimitives.store(sample).future()
      get <- database.optionalPrimitives.get(sample.pkey)
      update <- database.optionalPrimitives.updateColumns(updated)
      get2 <- database.optionalPrimitives.get(sample.pkey)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeRecord, afterRecord) => {
        beforeRecord shouldBe defined
        beforeRecord.value shouldEqual sample

        afterRecord shouldBe defined
        afterRecord.value shouldBe updated
      }
    }
  }

  it should "store an OptionalPrimitive in an optional table if some of the columns specified are null" in {
    val sample = OptionalPrimitive.empty
    val updated = gen[OptionalPrimitive].copy(pkey = sample.pkey, double = None, float = None)

    val chain = for {
      store <- database.optionalPrimitives.store(sample).future()
      get <- database.optionalPrimitives.get(sample.pkey)
      update <- database.optionalPrimitives.updateColumns(updated)
      get2 <- database.optionalPrimitives.get(sample.pkey)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeRecord, afterRecord) => {
        beforeRecord shouldBe defined
        beforeRecord.value shouldEqual sample

        afterRecord shouldBe defined
        afterRecord.value shouldBe updated
      }
    }
  }
}
