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

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{CQLQuery, QueryBuilderTest}
import com.websudos.phantom.tables.TestDatabase

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

        val qb = QueryBuilder.Select.Ordering.orderBy(orderings: _*).queryString

        qb shouldEqual "ORDER BY (test ASC, test_2 ASC, test_3 DESC)"
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
