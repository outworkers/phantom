package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.BasicTable

class DeleteQuerySerialisationTest extends QueryBuilderTest {

  "The DELETE query builder" - {
    "should generate table column deletion queries" - {
      "should create a delete query for a single column" - {
        BasicTable.delete(_.id)
      }
    }
  }
}
