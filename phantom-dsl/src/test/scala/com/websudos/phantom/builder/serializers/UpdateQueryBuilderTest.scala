package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.QueryBuilderTest

class UpdateQueryBuilderTest extends QueryBuilderTest {

  "The UPDATE query builder" - {

    "should allow specifying SET options" - {

      "should allowing updating the value of a column with a setTo clause" in {
        QueryBuilder.Update.setTo("a", "b").queryString shouldEqual "a = b"
      }

      "should allow specifying a set clause in an UPDATE query" in {
        QueryBuilder.Update
          .set(QueryBuilder.Update.setTo("a", "b"))
          .queryString shouldEqual "SET a = b"
      }

      "should allow specifying multiple SET clauses and chaining them" in {
        val c1 = QueryBuilder.Update.setTo("a", "b")
        val c2 = QueryBuilder.Update.setTo("c", "d")

        QueryBuilder.Update.chain(List(c1, c2)).queryString shouldEqual "a = b, c = d"

      }
    }

    "should allow specifying WHERE options" - {
    }

    "should allow specifying CAS options" - {

      "should allow specifying a single IF clause" in {
        QueryBuilder.Update.onlyIf(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "IF a = b"
      }

      "should allow specifying an AND clause" in {
        QueryBuilder.Update.and(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "AND a = b"
      }
    }
  }
}
