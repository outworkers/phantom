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

import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
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
