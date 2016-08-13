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
import com.websudos.phantom.builder.QueryBuilder
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.{Assertions, Matchers}

class UpdateQueryTest extends PhantomSuite with Matchers with Assertions with AsyncAssertions {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.insertSchema()
    database.optionalPrimitives.insertSchema()
    database.testTable.insertSchema()
  }

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[Primitive]

    val updatedRow = gen[Primitive].copy(pkey = row.pkey)

    val chain = for {
      store <- database.primitives.store(row).future()
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
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
      store <- database.testTable.store(row).future()
      a <- database.testTable.select.where(_.key eqs row.key).one
      u <- database.testTable.update
        .where(_.key eqs row.key)
        .modify(_.list setTo updatedRow.list)
        .and(_.setText setTo updatedRow.setText)
        .and(_.mapTextToText setTo updatedRow.mapTextToText)
        .and(_.setInt setTo updatedRow.setInt)
        .and(_.mapIntToText setTo updatedRow.mapIntToText).future()
      a2 <- database.testTable.select.where(_.key eqs row.key).one
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
      get <- database.optionalPrimitives.findByKey(sample.pkey)
      update <- database.optionalPrimitives.updateColumns(updated)
      get2 <- database.optionalPrimitives.findByKey(sample.pkey)
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
      get <- database.optionalPrimitives.findByKey(sample.pkey)
      update <- database.optionalPrimitives.updateColumns(updated)
      get2 <- database.optionalPrimitives.findByKey(sample.pkey)
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

  it should "store an OptionalPrimitive in an optional table with a single column being set to Some(_)" in {
    val sample = OptionalPrimitive.empty

    val chain = for {
      store <- database.optionalPrimitives.store(sample).future()
      get <- database.optionalPrimitives.findByKey(sample.pkey)
      update <- database.optionalPrimitives.update.where(_.pkey eqs sample.pkey)
          .modify(_.boolean setIfDefined Some(false))
          .future()
      get2 <- database.optionalPrimitives.findByKey(sample.pkey)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeRecord, afterRecord) => {
        beforeRecord shouldBe defined
        beforeRecord.value shouldEqual sample

        afterRecord shouldBe defined
        afterRecord.value shouldBe sample.copy(boolean = Some(false))
      }
    }
  }

  it should "store an OptionalPrimitive in an optional table using $setIfDefined and skip the update if None" in {
    val sample = gen[OptionalPrimitive]
    val updatedBool = false

    val query = database.optionalPrimitives.update.where(_.pkey eqs sample.pkey)
      .modify(_.boolean setIfDefined Some(updatedBool))
      .and(_.date setIfDefined None)

    query.setPart.list.size shouldEqual 1
    query.setPart.qb shouldEqual QueryBuilder.Update.set(QueryBuilder.Update.setTo(
      database.optionalPrimitives.boolean.name,
      updatedBool.toString
    ))

    val chain = for {
      store <- database.optionalPrimitives.store(sample).future()
      get <- database.optionalPrimitives.findByKey(sample.pkey)
      update <- query.future()
      get2 <- database.optionalPrimitives.findByKey(sample.pkey)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeRecord, afterRecord) => {
        beforeRecord shouldBe defined
        beforeRecord.value shouldEqual sample

        afterRecord shouldBe defined
        afterRecord.value shouldBe sample.copy(boolean = Some(false))
      }
    }
  }

  it should "store an OptionalPrimitive in an optional table using $setIfDefined and skip multiple updated" in {
    val sample = gen[OptionalPrimitive]
    val updatedBool = false

    val query = database.optionalPrimitives.update.where(_.pkey eqs sample.pkey)
      .modify(_.boolean setIfDefined Some(updatedBool))
      .and(_.date setIfDefined None)
      .and(_.double setIfDefined None)
      .and(_.inet setIfDefined None)


    query.setPart.list.size shouldEqual 1
    query.setPart.qb shouldEqual QueryBuilder.Update.set(QueryBuilder.Update.setTo(
      database.optionalPrimitives.boolean.name,
      updatedBool.toString
    ))

    val chain = for {
      store <- database.optionalPrimitives.store(sample).future()
      get <- database.optionalPrimitives.findByKey(sample.pkey)
      update <- query.future()
      get2 <- database.optionalPrimitives.findByKey(sample.pkey)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeRecord, afterRecord) => {
        beforeRecord shouldBe defined
        beforeRecord.value shouldEqual sample

        afterRecord shouldBe defined
        afterRecord.value shouldBe sample.copy(boolean = Some(false))
      }
    }
  }

  it should "allow using a timestamp clause with a normal assignments query" in {
    val row = gen[Primitive]

    val sample = gen[Primitive].copy(pkey = row.pkey)
    val timestamp = DateTime.now(DateTimeZone.UTC).plusMinutes(3)

    val chain = for {
      store <- database.primitives.store(row).future()
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setTo sample.long)
        .and(_.boolean setTo sample.boolean)
        .and(_.bDecimal setTo sample.bDecimal)
        .and(_.double setTo sample.double)
        .and(_.float setTo sample.float)
        .and(_.inet setTo sample.inet)
        .and(_.int setTo sample.int)
        .and(_.date setTo sample.date)
        .and(_.uuid setTo sample.uuid)
        .and(_.bi setTo sample.bi)
        .timestamp(timestamp)
        .future()
      a2 <- TestDatabase.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) {
      case (res1, res2) => {
        res1.value shouldEqual row
        res2.value shouldEqual sample
      }
    }
  }
}
