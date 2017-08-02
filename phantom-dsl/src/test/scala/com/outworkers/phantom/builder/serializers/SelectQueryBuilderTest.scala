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

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._

class SelectQueryBuilderTest extends QueryBuilderTest {

  "The Select query builder" - {
    "should allow serialising SELECT query clauses" - {

      "should allow selecting a sequence of columns" in {
        val qb = QueryBuilder.Select.select("t", "k", "test", "test2", "test3").queryString

        qb shouldEqual "SELECT test, test2, test3 FROM k.t"
      }

      "should create a SELECT * query if no specific columns are selected" in {
        val qb = QueryBuilder.Select.select("t", "k").queryString

        qb shouldEqual "SELECT * FROM k.t"
      }

      "should serialize a SELECT COUNT query given a table name and a keyspace" in {
        val qb = QueryBuilder.Select.count("t", "k").queryString

        qb shouldEqual "SELECT COUNT(*) FROM k.t"
      }

      "should serialise a SELECT DISTINCT query from a table, keyspace and column sequence" in {
        val qb = QueryBuilder.Select.distinct("t", "k", "test", "test1").queryString

        qb shouldEqual "SELECT DISTINCT test, test1 FROM k.t"
      }
    }

    "should allow defining an ordering clause on a selection" - {
      "should allow specifying ASCENDING ordering" in {
        val qb = QueryBuilder.Select.Ordering.ascending("test").queryString
        qb shouldEqual "test ASC"
      }

      "should allow specifying DESCENDING ordering" in {
        val qb = QueryBuilder.Select.Ordering.descending("test").queryString
        qb shouldEqual "test DESC"
      }

      "should chain an ORDER BY clause to a CQLQuery" in {
        val root = TestDatabase.basicTable.select.all().qb

        val qb = QueryBuilder.Select.Ordering.orderBy(
          root, QueryBuilder.Select.Ordering.descending(TestDatabase.basicTable.id.name)
        )

        qb.queryString shouldEqual s"SELECT * FROM phantom.basicTable ORDER BY ${TestDatabase.basicTable.id.name} DESC"
      }

      "should allow specifying multiple orderBy clauses in a single select query" in {

        val orderings = Seq(
          QueryBuilder.Select.Ordering.ascending("test"),
          QueryBuilder.Select.Ordering.ascending("test_2"),
          QueryBuilder.Select.Ordering.descending("test_3")
        )

        val qb = QueryBuilder.Select.Ordering.orderBy(orderings).queryString

        qb shouldEqual "ORDER BY test ASC, test_2 ASC, test_3 DESC"
      }
    }

    "should correctly serialise token operations" - {
      "should serialise a single token op" in {
        val sample = genList[String](1)
        val qs = sample.mkString(", ")

        val query = QueryBuilder.Where.token(sample: _*).queryString
        query shouldEqual s"TOKEN ($qs)"
      }

      "should serialise a two token op" in {
        val sample = genList[String](2)
        val qs = sample.mkString(", ")

        val query = QueryBuilder.Where.token(sample: _*).queryString
        query shouldEqual s"TOKEN ($qs)"
      }

      "should serialise a three token op" in {
        val sample = genList[String](3)
        val qs = sample.mkString(", ")

        val query = QueryBuilder.Where.token(sample: _*).queryString
        query shouldEqual s"TOKEN ($qs)"
      }
    }

    "should allow specifying Selection options" - {

      "should allow specifying an ALLOW FILTERING clause on an existing query" in {
        val qb = CQLQuery("SELECT * FROM k.t")
        QueryBuilder.Select.allowFiltering(qb).queryString shouldEqual "SELECT * FROM k.t ALLOW FILTERING"
      }

      "should allow creating a dateOf select clause" in {
        QueryBuilder.Select.dateOf("test").queryString shouldEqual "dateOf(test)"
      }

      "should allow creating a blobAsText select clause from a string" in {
        QueryBuilder.Select.blobAsText("test").queryString shouldEqual "blobAsText(test)"
      }

      "should allow creating a blobAsText select clause from another CQLQuery" in {
        QueryBuilder.Select.blobAsText(CQLQuery("test")).queryString shouldEqual "blobAsText(test)"
      }

    }
  }

}
