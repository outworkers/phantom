package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.QueryBuilderTest

class DeleteQueryBuilderTest extends QueryBuilderTest {
  "The DELETE query builder" - {

    "should allow specifying column delete queries" - {
      val qb = QueryBuilder.Delete.deleteColumn("table", "col").queryString
      qb shouldEqual "DELETE col FROM table"
    }

    "should allow specifying full delete queries" - {
      val qb = QueryBuilder.Delete.delete("table").queryString
      qb shouldEqual "DELETE FROM table"
    }
  }
}
