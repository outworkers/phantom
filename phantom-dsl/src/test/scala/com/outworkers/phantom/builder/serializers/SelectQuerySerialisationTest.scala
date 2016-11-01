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
package com.outworkers.phantom.builder.serializers

import java.util.Date

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.testing._

import scala.collection.SeqLike

class SelectQuerySerialisationTest extends QueryBuilderTest {

  val BasicTable = TestDatabase.basicTable
  val ArticlesByAuthor = TestDatabase.articlesByAuthor
  val TimeSeriesTable = TestDatabase.timeSeriesTable

  protected[this] val limit = 5

  "The select query builder" - {

    "should serialize distinct clauses" - {
      "should correctly append a * if there is no column selection provided to a distinct clause" in {
        val id = gen[UUID]

        val qb = BasicTable.select.distinct().where(_.id eqs id).limit(limit).queryString

        qb shouldEqual s"SELECT DISTINCT * FROM phantom.basicTable WHERE id = ${id.toString} LIMIT 5;"
      }

      "should correctly append a column selection to a distinct clause" in {
        val id = gen[UUID]

        val qb = TestDatabase.tableWithCompositeKey.select(_.id, _.second_part)
          .distinct()
          .where(_.id eqs id)
          .limit(limit).queryString

        qb shouldEqual s"SELECT DISTINCT id, second_part FROM phantom.tableWithCompositeKey WHERE id = ${id.toString} LIMIT 5;"
      }
    }

    "should allow serialising JSON selection clauses" - {
      "should allow a SELECT JSON * syntax" in {
        val id = gen[UUID]

        val qb = BasicTable.select.json().where(_.id eqs id).queryString

        qb shouldEqual s"SELECT JSON * FROM phantom.basicTable WHERE id = ${id.toString};"
      }

      "should allow a SELECT JSON col1, col2, .. syntax" in {
        val id = gen[UUID]

        val qb = BasicTable.select(_.id, _.id2).json().where(_.id eqs id).queryString

        qb shouldEqual s"SELECT JSON id, id2 FROM phantom.basicTable WHERE id = ${id.toString};"
      }
    }

    "should allow serialising USING clause syntax" - {

      "should allow specifying USING IGNORE NULLS" in {
        val id = gen[UUID]
        val qb = BasicTable.select.where(_.id eqs id).using(ignoreNulls).queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} USING IGNORE_NULLS;"
      }
    }

    "should serialize combinations of limits and allow filtering clauses " - {

      "serialise an allow filtering clause in the init position" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).allowFiltering().limit(limit).queryString

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} LIMIT 5 ALLOW FILTERING;"
      }

      "serialize an allow filtering clause specified after a limit query" in {
        val id = gen[UUID]

        val qb = BasicTable.select.where(_.id eqs id).limit(limit).allowFiltering().queryString

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

        qb shouldEqual s"SELECT * FROM phantom.basicTable WHERE id = ${id.toString} ORDER BY id2 DESC, id3 ASC;"
      }

      "a maxTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb should startWith (s"SELECT * FROM phantom.timeSeriesTable WHERE unixTimestamp > maxTimeuuid(")
      }

      "a maxTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > maxTimeuuid(date)).queryString

        qb should startWith ("SELECT * FROM phantom.timeSeriesTable WHERE unixTimestamp > maxTimeuuid(")
      }

      "a minTimeuuid comparison clause" in {
        val date = new Date

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb should startWith (s"SELECT * FROM phantom.timeSeriesTable WHERE unixTimestamp > minTimeuuid(")
      }

      "a minTimeuuid comparison clause with a DateTime object" in {
        val date = new DateTime

        val qb = TimeSeriesTable.select.where(_.timestamp > minTimeuuid(date)).queryString

        qb should startWith (s"SELECT * FROM phantom.timeSeriesTable WHERE unixTimestamp > minTimeuuid(")
      }

      "a multiple column token clause" in {
        val qb = ArticlesByAuthor.select.where(t => {
          token(gen[UUID], gen[UUID]) > token(t.author_id, t.category)
        }).queryString
        info(qb)
      }

      "a single column token clause" in {
        val qb = ArticlesByAuthor.select.where(_.author_id gtToken gen[UUID]).queryString
        info(qb)
      }

      "a consistency level setting" in {
        val id = gen[UUID]

        val qb = ArticlesByAuthor.select.where(_.author_id gtToken id)
          .consistencyLevel_=(ConsistencyLevel.EACH_QUORUM)
          .queryString

        if (session.protocolConsistency) {
          qb shouldEqual s"SELECT * FROM phantom.articlesByAuthor WHERE TOKEN (author_id) > TOKEN($id);"
        } else {
          qb shouldEqual s"SELECT * FROM phantom.articlesByAuthor WHERE TOKEN (author_id) > TOKEN($id) USING CONSISTENCY EACH_QUORUM;"
        }
      }

      "a single dateOf column apply" in {
        val id = UUIDs.timeBased()
        val qb = TestDatabase.timeuuidTable.select
          .function(t => dateOf(t.id))
          .where(_.id eqs id)
          .qb.queryString

        qb shouldEqual s"SELECT dateOf(id) FROM phantom.timeUUIDTable WHERE id = $id"
      }
    }
  }
}
