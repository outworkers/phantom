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
package com.outworkers.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.RootConnector
import com.twitter.scrooge.CompactThriftSerializer
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.thrift._

case class Output(
  id: UUID,
  name: String,
  struct: ThriftTest,
  thriftSet: Set[ThriftTest],
  thriftList: List[ThriftTest],
  thriftMap: Map[String, ThriftTest],
  optThrift: Option[ThriftTest]
)

sealed class ThriftColumnTable extends CassandraTable[ConcreteThriftColumnTable, Output] {

  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object ref extends ThriftColumn[ConcreteThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftSet extends ThriftSetColumn[ConcreteThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftList extends ThriftListColumn[ConcreteThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftMap extends ThriftMapColumn[ConcreteThriftColumnTable, Output, String, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object optionalThrift extends OptionalThriftColumn[ConcreteThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }
}

abstract class ConcreteThriftColumnTable extends ThriftColumnTable with RootConnector {
  override val tableName = "thrift_column_table"

  def store(sample: Output): InsertQuery.Default[ConcreteThriftColumnTable, Output] = {
    insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample.struct)
      .value(_.thriftSet, sample.thriftSet)
      .value(_.thriftList, sample.thriftList)
  }
}


sealed class ThriftIndexedTable extends CassandraTable[ConcreteThriftIndexedTable, Output] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  object ref extends ThriftColumn[ConcreteThriftIndexedTable, Output, ThriftTest](this) with PartitionKey {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftSet extends ThriftSetColumn[ConcreteThriftIndexedTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftList extends ThriftListColumn[ConcreteThriftIndexedTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftMap extends ThriftMapColumn[ConcreteThriftIndexedTable, Output, String, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object optionalThrift extends OptionalThriftColumn[ConcreteThriftIndexedTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }
}

abstract class ConcreteThriftIndexedTable extends ThriftIndexedTable with RootConnector {
  override val tableName = "thrift_indexed_table"

  def store(sample: Output): InsertQuery.Default[ConcreteThriftIndexedTable, Output] = {
    insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample.struct)
      .value(_.thriftSet, sample.thriftSet)
      .value(_.thriftList, sample.thriftList)
      .value(_.optionalThrift, sample.optThrift)
      .value(_.thriftMap, sample.thriftMap)
  }
}

class ThriftDatabase(override val connector: CassandraConnection) extends Database[ThriftDatabase](connector) {
  object thriftColumnTable extends ConcreteThriftColumnTable with Connector
  object thriftIndexedTable extends ConcreteThriftIndexedTable with Connector
}

object ThriftDatabase extends ThriftDatabase(connectors.ContactPoint.local.keySpace("phantom"))