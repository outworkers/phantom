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
import com.outworkers.phantom.dsl._

class SASIQueriesTest extends PhantomSuite {

  it should "automatically find SASI indexed columns " in {
    val sasiColumns = database.sasiIndexedArticles.sasiIndexes
    database.sasiIndexedArticles.sasiQueries().queries.size shouldEqual 1
    sasiColumns.exists(_.name == database.sasiIndexedArticles.orderId.name) shouldEqual true
  }

  it should "generate a correct query for a SASI index" in {
    val queries = database.sasiIndexedArticles.sasiQueries().queries
    queries.size shouldEqual 1

    val qs = queries.headOption.value.qb.queryString
    val expected = "CREATE CUSTOM INDEX IF NOT EXISTS sASIIndexedArticles_orderId_idx ON phantom.sASIIndexedArticles(orderId) " +
      "USING 'org.apache.cassandra.index.sasi.SASIIndex' WITH OPTIONS = " +
      "{'mode': 'PREFIX', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'tokenization_enable_stemming': 'true'}"
    qs shouldEqual expected
  }


  it should "automatically find multiple SASI indexed columns " in {
    database.multiSasiTable.sasiQueries().queries.size shouldEqual 3
  }

  it should "allow using a prefix clause on a Mode.Contains Text column" in {
    val qb = database.multiSasiTable.select.where(_.name like prefix("test")).queryString

    qb shouldEqual s"SELECT * FROM ${db.space.name}.${database.multiSasiTable.tableName} WHERE name LIKE 'test%';"
  }

  it should "allow using a suffix clause on a Mode.Contains Text column" in {
    val qb = database.multiSasiTable.select.where(_.name like suffix("test")).queryString

    qb shouldEqual s"SELECT * FROM ${db.space.name}.${database.multiSasiTable.tableName} WHERE name LIKE '%test';"
  }

  it should "allow using a contains clause on a Mode.Contains Text column" in {
    val qb = database.multiSasiTable.select.where(_.name like contains("test")).queryString

    qb shouldEqual s"SELECT * FROM ${db.space.name}.${database.multiSasiTable.tableName} WHERE name LIKE '%test%';"
  }

  it should "allow using a prefix clause on a Mode.Prefix Text column" in {
    val qb = database.multiSasiTable.select.where(_.phoneNumber like prefix("078")).queryString

    qb shouldEqual s"SELECT * FROM ${db.space.name}.${database.multiSasiTable.tableName} WHERE phoneNumber LIKE '078%';"
  }

  it should "not allow like queries in Mode.Sparse" in {
    val pre = 55
    "db.multiSasiTable.select.where(_.customers like(prefix(pre)) pre).fetch()" shouldNot compile
  }

  it should "not allow using a suffix clause on a Mode.Sparse Text column" in {
    """database.multiSasiTable.select.where(_.description like prefix("test"))""" shouldNot compile
  }

  it should "not allow using a contains clause on a Mode.Sparse Text column" in {
    """database.multiSasiTable.select.where(_.customers like contains(5))""" shouldNot compile
  }

  it should "not allow using a suffix clause on a Mode.Prefix Text column" in {
    """database.multiSasiTable.select.where(_.phoneNumber like prefix("078"))""" shouldNot compile
  }

  it should "not allow using a contains clause on a Mode.Prefix Text column" in {
    """database.multiSasiTable.select.where(_.phoneNumber like contains("078"))""" shouldNot compile
  }
}
