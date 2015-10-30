package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.TimeSeriesTable
import com.websudos.phantom.dsl._

class CreateQuerySerialisationTest extends QueryBuilderTest {
  "The CREATE query builder" - {
    "should generate clustering keys for a schema queries" - {
      "generate a descending order clustering key on a table with a single clustering key" in {
        val qb = TimeSeriesTable.create.qb.queryString

        qb shouldEqual """CREATE TABLE phantom."TimeSeriesTable" (id uuid, name text, timestamp timestamp, """ +
          """PRIMARY KEY (id, timestamp)) WITH CLUSTERING ORDER BY (timestamp DESC)"""
      }
    }
  }
}
