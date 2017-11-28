/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{Assertions, Inside, Matchers}

class UpdateQueryTest extends PhantomSuite with Matchers with Assertions with Inside {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.primitives.createSchema()
    database.optionalPrimitives.createSchema()
    database.testTable.createSchema()
  }

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[PrimitiveRecord]

    val updatedRow = gen[PrimitiveRecord].copy(pkey = row.pkey)

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
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual updatedRow
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

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual updatedRow
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

    whenReady(chain) { case (beforeRecord, afterRecord) =>
      beforeRecord shouldBe defined
      beforeRecord.value shouldEqual sample

      afterRecord shouldBe defined
      afterRecord.value shouldBe updated
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

    whenReady(chain) { case (beforeRecord, afterRecord) =>
      beforeRecord shouldBe defined
      beforeRecord.value shouldEqual sample

      afterRecord shouldBe defined
      afterRecord.value shouldBe updated
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

    whenReady(chain) { case (beforeRecord, afterRecord) =>
      beforeRecord shouldBe defined
      beforeRecord.value shouldEqual sample

      afterRecord shouldBe defined
      afterRecord.value shouldBe sample.copy(boolean = Some(false))
    }
  }

  it should "store an OptionalPrimitive in an optional table using $setIfDefined and skip the update if None" in {
    val sample = gen[OptionalPrimitive]
    val updatedBool = false

    val query = database.optionalPrimitives.update.where(_.pkey eqs sample.pkey)
      .modify(_.boolean setIfDefined Some(updatedBool))
      .and(_.date setIfDefined None)

    query.setPart.queries.size shouldEqual 1
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

    whenReady(chain) { case (beforeRecord, afterRecord) =>
      beforeRecord shouldBe defined
      beforeRecord.value shouldEqual sample

      afterRecord shouldBe defined
      afterRecord.value shouldBe sample.copy(boolean = Some(false))
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


    query.setPart.queries.size shouldEqual 1
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

    whenReady(chain) { case (beforeRecord, afterRecord) =>
      beforeRecord shouldBe defined
      beforeRecord.value shouldEqual sample

      afterRecord shouldBe defined
      afterRecord.value shouldBe sample.copy(boolean = Some(false))
    }
  }

  it should "allow using a timestamp clause with a normal assignments query" in {
    val row = gen[PrimitiveRecord]

    val sample = gen[PrimitiveRecord].copy(pkey = row.pkey)
    val t1 = DateTime.now(DateTimeZone.UTC)
    val t2 = DateTime.now(DateTimeZone.UTC).plusMinutes(1)

    val chain = for {
      store <- database.primitives.store(row).timestamp(t1).future()
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
        .timestamp(t2)
        .future()
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) {
      case (res1, res2) => {
        res1.value shouldEqual row
        res2.value.long shouldEqual sample.long
        res2.value.bi shouldEqual sample.bi
        res2.value.uuid shouldEqual sample.uuid
        res2.value.float shouldEqual sample.float
        res2.value.inet shouldEqual sample.inet
        res2.value.date shouldEqual sample.date
        res2.value.int shouldEqual sample.int
        res2.value.boolean shouldEqual sample.boolean
        res2.value.bDecimal shouldEqual sample.bDecimal
        res2.value.double shouldEqual sample.double
      }
    }
  }

  it should "allow using initiating a setIfDefined chain with a None" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[PrimitiveRecord]

    val updatedRow = gen[PrimitiveRecord].copy(pkey = row.pkey)

    val chain = for {
      store <- database.primitives.store(row).future()
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setIfDefined None)
        .and(_.boolean setTo updatedRow.boolean)
        .and(_.bDecimal setTo updatedRow.bDecimal)
        .and(_.double setIfDefined None)
        .and(_.float setTo updatedRow.float)
        .and(_.inet setTo updatedRow.inet)
        .and(_.int setTo updatedRow.int)
        .and(_.date setTo updatedRow.date)
        .and(_.uuid setTo updatedRow.uuid)
        .and(_.bi setTo updatedRow.bi)
        .future()
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual updatedRow.copy(double = row.double, long = row.long)
    }
  }

  it should "allow using setIfDefined on non optional columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[PrimitiveRecord]

    val updatedRow = gen[PrimitiveRecord].copy(pkey = row.pkey)

    val chain = for {
      store <- database.primitives.store(row).future()
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setIfDefined Some(updatedRow.long))
        .and(_.boolean setTo updatedRow.boolean)
        .and(_.bDecimal setTo updatedRow.bDecimal)
        .and(_.double setIfDefined None)
        .and(_.float setTo updatedRow.float)
        .and(_.inet setTo updatedRow.inet)
        .and(_.int setTo updatedRow.int)
        .and(_.date setTo updatedRow.date)
        .and(_.uuid setTo updatedRow.uuid)
        .and(_.bi setTo updatedRow.bi)
        .future()
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual updatedRow.copy(double = row.double)
    }
  }

  it should "allow using succeedAnyway if the update query is empty" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[PrimitiveRecord]

    val chain = for {
      store <- database.primitives.storeRecord(row)
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setIfDefined None)
        .and(_.boolean setIfDefined None)
        .and(_.bDecimal setIfDefined None)
        .and(_.double setIfDefined None)
        .and(_.float setIfDefined None)
        .and(_.inet setIfDefined None)
        .and(_.int setIfDefined None)
        .and(_.date setIfDefined None)
        .and(_.uuid setIfDefined None)
        .and(_.bi setIfDefined None)
        .succeedAnyway()
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual row
    }
  }

  it should "allow using succeedAnyway if the update query has set clauses defined" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[PrimitiveRecord]

    val updatedRow = gen[PrimitiveRecord].copy(pkey = row.pkey)

    val chain = for {
      store <- database.primitives.store(row).future()
      a <- database.primitives.select.where(_.pkey eqs row.pkey).one
      u <- database.primitives.update.where(_.pkey eqs row.pkey)
        .modify(_.long setIfDefined Some(updatedRow.long))
        .and(_.boolean setTo updatedRow.boolean)
        .and(_.bDecimal setTo updatedRow.bDecimal)
        .and(_.double setIfDefined None)
        .and(_.float setTo updatedRow.float)
        .and(_.inet setTo updatedRow.inet)
        .and(_.int setTo updatedRow.int)
        .and(_.date setTo updatedRow.date)
        .and(_.uuid setTo updatedRow.uuid)
        .and(_.bi setTo updatedRow.bi)
        .succeedAnyway()
      a2 <- database.primitives.select.where(_.pkey eqs row.pkey).one
    } yield (a, a2)

    whenReady(chain) { case (res1, res2) =>
      res1.value shouldEqual row
      res2.value shouldEqual updatedRow.copy(double = row.double)
    }
  }
}
