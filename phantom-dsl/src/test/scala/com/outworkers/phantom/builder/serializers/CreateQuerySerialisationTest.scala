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

import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase

class CreateQuerySerialisationTest extends QueryBuilderTest {

  "The CREATE query builder" - {
    "should generate clustering keys for a schema queries" - {
      "generate a descending order clustering key on a table with a single clustering key" in {
        val qb = TestDatabase.timeSeriesTable.create.qb.queryString

        qb shouldEqual "CREATE TABLE phantom.timeSeriesTable (id uuid, name text, unixTimestamp timestamp, " +
          "PRIMARY KEY (id, unixTimestamp)) WITH CLUSTERING ORDER BY (unixTimestamp DESC)"
      }

      "retrieve clustering columns in the order they are written" in {
        val qb = TestDatabase.clusteringTable.create.qb.queryString

        qb shouldEqual "CREATE TABLE phantom.clusteringTable (id uuid, id2 uuid, id3 uuid, placeholder text, " +
          "PRIMARY KEY (id, id2, id3)) WITH CLUSTERING ORDER BY (id2 ASC, id3 DESC)"
      }

      "retrieve clustering columns in the order they are written for three clustering columns" in {
        val qb = TestDatabase.complexClusteringTable.create.qb.queryString

        qb shouldEqual "CREATE TABLE phantom.complexClusteringTable (id uuid, id2 uuid, id3 uuid, placeholder text, " +
          "PRIMARY KEY (id, id2, id3, placeholder)) WITH CLUSTERING ORDER BY (id2 ASC, id3 DESC, placeholder DESC)"
      }
    }
  }
}
