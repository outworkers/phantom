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

        qb.queryString shouldEqual s"SELECT * FROM phantom.BasicTable ORDER BY ${TestDatabase.basicTable.id.name} DESC"
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
