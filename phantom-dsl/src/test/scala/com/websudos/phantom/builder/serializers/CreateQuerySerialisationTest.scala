package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase

class CreateQuerySerialisationTest extends QueryBuilderTest {

  "The CREATE query builder" - {
    "should generate clustering keys for a schema queries" - {
      "generate a descending order clustering key on a table with a single clustering key" in {
        val qb = TestDatabase.timeSeriesTable.create.qb.queryString

        qb shouldEqual "CREATE TABLE phantom.timeSeriesTable (id uuid, name text, timestamp timestamp, " +
          "PRIMARY KEY (id, timestamp)) WITH CLUSTERING ORDER BY (timestamp DESC)"
      }
    }
  }
}
