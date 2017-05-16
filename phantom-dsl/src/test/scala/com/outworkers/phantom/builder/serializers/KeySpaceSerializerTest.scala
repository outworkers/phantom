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

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

class KeySpaceSerializerTest extends QuerySerializationTest {

  it should "create a simple keyspace creation query" in {
    val query = KeySpaceSerializer("test").ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy)
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy'}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }

  it should "create a simple keyspace creation query using the package augmentation" in {
    val query = KeySpace("test").builder.ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy)
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy'}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }

  it should "create a keyspace query using QueryBuilder.keyspace" in {
    val sample = gen[KeySpace]
    QueryBuilder.keyspace(sample).ifNotExists().queryString shouldEqual s"CREATE KEYSPACE IF NOT EXISTS ${sample.name}"
  }

  it should "create allow specifying a simple strategy " in {
    val query = QueryBuilder.keyspace("test").ifNotExists()
      .`with`(replication eqs SimpleStrategy)
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy'}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }

  it should "allow creating a simple strategy with a replication factor defined" in {
    val query = QueryBuilder.keyspace("test").ifNotExists()
      .`with`(replication eqs SimpleStrategy.replication_factor(2))
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 2}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }

  it should "create a simple keyspace using NetworkTopology and data center specific settings" in {
    val query = QueryBuilder.keyspace("test").ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy
        .data_center("data1", 2)
        .data_center("data2", 3)
      )
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy', 'data1': 2, 'data2': 3}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
  }
}
