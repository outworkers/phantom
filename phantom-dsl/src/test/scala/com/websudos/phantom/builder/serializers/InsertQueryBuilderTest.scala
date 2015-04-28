package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{CQLQuery, QueryBuilderTest}

class InsertQueryBuilderTest extends QueryBuilderTest {

  "The INSERT query builder" - {

    "should allow appending columns and values to a query" - {

      "should serialize a sequence of appended values" in {
        val query = QueryBuilder.Insert.values(List(CQLQuery("a"), CQLQuery("b"))).queryString

        query shouldEqual "VALUES(a, b)"
      }

      "should serialize a sequence of value additions" in {

      }

    }
  }
}
