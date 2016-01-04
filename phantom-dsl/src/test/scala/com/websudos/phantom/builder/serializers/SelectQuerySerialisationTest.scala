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
package com.websudos.phantom.builder.serializers

import java.util.Date

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.{ArticlesByAuthor, BasicTable, TestDatabase}
import com.websudos.phantom.dsl._
import com.websudos.util.testing._
import org.scalatest.FreeSpec

class SelectQuerySerialisationTest extends QueryBuilderTest {

  val BasicTable = TestDatabase.basicTable
  val ArticlesByAuthor = TestDatabase.articlesByAuthor
  val TimeSeriesTable = TestDatabase.timeSeriesTable

  "The select query builder" - {
    "should serialize " - {

      "serialise an allow filtering clause in the init position" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).allowFiltering().limit(5).queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "serialize an allow filtering clause specified after a limit query" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).limit(5).allowFiltering().queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "serialize a single ordering clause" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).orderBy(_.id2.desc).queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} ORDER BY id2 DESC;"
      }

      "serialize an ordering by multiple columns" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).orderBy(_.id2.desc, _.id3.asc).queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} ORDER BY (id2 DESC, id3 ASC);"
      }

      "a maxTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > maxTimeuuid(${DateIsPrimitive.asCql(date)});"
      }

      "a maxTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > maxTimeuuid(${DateTimeIsPrimitive.asCql(date)});"
      }

      "a minTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > minTimeuuid(${DateIsPrimitive.asCql(date)});"
      }

      "a minTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb shouldEqual s"SELECT * FROM phantom.TimeSeriesTable WHERE unixTimestamp > minTimeuuid(${DateTimeIsPrimitive.asCql(date)});"
      }

      "a multiple column token clause" in {
        val qb = ArticlesByAuthor.select.where(t => { token(gen[UUID], gen[UUID]) > token(t.author_id, t.category) }).queryString
      }

      "a single column token clause" in {
        val qb = ArticlesByAuthor.select.where(_.author_id gtToken gen[UUID]).queryString
        info(qb)
      }

      "a consistency level setting" in {
        val qb = ArticlesByAuthor.select.where(_.author_id eqs gen[UUID])
          .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM)
          .queryString

        Console.println(qb)
      }

      "a single dateOf column apply" in {
        //val qb = ArticlesByAuthor.select.where(t => dateOf(t))
      }
    }
  }

}
