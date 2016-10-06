/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.dsl._
import shapeless.{Generic, HList, HNil, LabelledGeneric}

import scala.util.{Failure, Success, Try}

class KeySpaceSerializerTest extends QuerySerializationTest {

  it should "create a simple keyspace creation query" in {
    val query = QueryBuilder.keyspace("test").ifNotExists()
      .`with`(replication eqs NetworkTopologyStrategy)
      .and(durable_writes eqs true)
      .qb.queryString

    val expected = "CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'NetworkTopologyStrategy'}" +
      " AND DURABLE_WRITES = true"

    query shouldEqual expected
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
