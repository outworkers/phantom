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
package com.outworkers.phantom.database

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._

object basicDb extends BasicDatabase

class DatabaseTest extends PhantomSuite {

  it should "instantiate a database and collect references to the tables" in {
    basicDb.tables.size shouldEqual 4
  }

  it should "automatically generate the CQL schema and initialise tables " in {
    whenReady(basicDb.createAsync()) { res =>
      res.nonEmpty shouldEqual true
    }
  }

  it should "respect any auto-creation options specified for the particular table" in {
    val queries = basicDb.autocreate().queries.map(_.qb)

    val target = basicDb.recipes.autocreate(space).qb

    queries should contain (target)
  }

  it should "automatically drop a table using the autodrop method" in {
    val chain = for {
      _ <- basicDb.createAsync()
      _ <- basicDb.autodrop().future()
      exists <- if (cassandraVersion.value >= Version.`3.0.0`) {
        cql(s"SELECT table_name FROM system_schema.tables WHERE keyspace_name ='${basicDb.enumTable.tableName}' LIMIT 1").future()
      } else {
        cql(s"SELECT columnfamily_name FROM system.schema_columnfamilies WHERE keyspace_name ='${basicDb.enumTable.tableName}' LIMIT 1").future()
      }
    } yield exists

    whenReady(chain) { res =>
      Option(res.one()) shouldBe empty
    }
  }

  it should "automatically drop a table using the dropAsync method" in {
    val chain = for {
      _ <- basicDb.createAsync()
      _ <- basicDb.dropAsync()
      exists <- if (cassandraVersion.value >= Version.`3.0.0`) {
        cql(s"SELECT table_name FROM system_schema.tables WHERE keyspace_name ='${basicDb.enumTable.tableName}' LIMIT 1").future()
      } else {
        cql(s"SELECT columnfamily_name FROM system.schema_columnfamilies WHERE keyspace_name ='${basicDb.enumTable.tableName}' LIMIT 1").future()
      }
    } yield exists

    whenReady(chain) { res =>
      Option(res.one()) shouldBe empty
    }
  }
}
