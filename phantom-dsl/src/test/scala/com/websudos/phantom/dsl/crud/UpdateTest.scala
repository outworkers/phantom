/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl.crud

import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class UpdateTest extends PhantomCassandraTestSuite with Matchers with Assertions with AsyncAssertions {

  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = gen[Primitive]

    val updatedRow = gen[Primitive].copy(pkey = row.pkey)
    Primitives.insertSchema()
    val rcp = Primitives.insert
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
        .value(_.bi, row.bi).future() flatMap {
        _ => {
          for {
            a <- Primitives.select.where(_.pkey eqs row.pkey).one
            b <- Primitives.select.fetch
            u <- Primitives.update.where(_.pkey eqs row.pkey)
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
            a2 <- Primitives.select.where(_.pkey eqs row.pkey).one
            b2 <- Primitives.select.fetch

          } yield (
            a,
            b,
            a2,
            b2
          )
        }
      }

    rcp successful {
      r => {
        r._1.isDefined shouldEqual true
        r._1.get shouldEqual row
        r._2 contains row shouldEqual true

        r._3.isDefined shouldEqual true
        r._3.get shouldEqual updatedRow
        r._4 contains updatedRow shouldEqual true
      }
    }
  }

  it should "work fine with List, Set, Map" in {

    val row = gen[TestRow]

    val updatedRow = row.copy(
      list = List("new"),
      setText = Set("newSet"),
      mapTextToText =  Map("n" -> "newVal"),
      setInt = Set(3,4,7),
      mapIntToText = Map (-1 -> "&&&")
    )
    scala.util.Right(5)

    TestTable.insertSchema()

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .future() flatMap {
        _ => for {
        a <-TestTable.select.where(_.key eqs row.key).one
        b <-TestTable.select.fetch
        u <- TestTable.update
          .where(_.key eqs row.key)
          .modify(_.list setTo updatedRow.list)
          .and(_.setText setTo updatedRow.setText)
          .and(_.mapTextToText setTo updatedRow.mapTextToText)
          .and(_.setInt setTo updatedRow.setInt)
          .and(_.mapIntToText setTo updatedRow.mapIntToText).future()
        a2 <- TestTable.select.where(_.key eqs row.key).one
        b2 <- TestTable.select.fetch
        } yield (
          a.get === row,
          b.contains(row),
          a2.get === updatedRow,
          b2.contains(updatedRow)
        )
    }
    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
        assert(r._3)
        assert(r._4)
      }
    }
  }
}
