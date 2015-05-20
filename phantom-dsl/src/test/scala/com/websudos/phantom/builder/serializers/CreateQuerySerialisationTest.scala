package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.TimeSeriesTable

class CreateQuerySerialisationTest extends QueryBuilderTest {
  "The CREATE query builder" - {
    "should generate clustering keys for a schema queries" - {
      val qb = TimeSeriesTable.defineTableKey()
    }
  }
}
