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
package com.outworkers.phantom.builder.query.sasi

import com.outworkers.phantom.PhantomSuite

class SASIQueriesTest extends PhantomSuite {

  it should "automatically find SASI indexed columns " in {
    val sasiColumns = database.sasiIndexedArticles.sasiIndexes
    database.sasiIndexedArticles.sasiQueries().queries.size shouldEqual 1
    sasiColumns.exists(_.name == database.sasiIndexedArticles.orderId.name) shouldEqual true
  }

  it should "generate a correct query for a SASI index" in {
    val queries = database.sasiIndexedArticles.sasiQueries().queries
    queries.size shouldEqual 1

    val qs = queries.headOption.value.queryString
    val expected = "CREATE CUSTOM INDEX sASIIndexedArticles_orderId_idx ON phantom.sASIIndexedArticles(orderId) USING 'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = {'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_enable_stemming': 'true'}"
    qs shouldEqual expected
  }
}
