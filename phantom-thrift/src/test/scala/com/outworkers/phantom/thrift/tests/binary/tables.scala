/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
package com.outworkers.phantom.thrift.tests.binary

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.Connector
import com.outworkers.phantom.thrift.models._
import com.outworkers.phantom.thrift.binary._
import com.outworkers.phantom.thrift.tests.ThriftRecord
import com.outworkers.phantom.thrift.tests.compact.ThriftDatabase

abstract class ThriftColumnTable extends Table[ThriftColumnTable, ThriftRecord] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object ref extends Col[ThriftTest]

  object thriftSet extends SetColumn[ThriftTest]

  object thriftList extends ListColumn[ThriftTest]

  object thriftMap extends MapColumn[String, ThriftTest]

  object optionalThrift extends OptionalCol[ThriftTest]
}

abstract class ThriftIndexedTable extends Table[ThriftIndexedTable, ThriftRecord] {

  object id extends UUIDColumn
  object name extends StringColumn

  object ref extends Col[ThriftTest] with PartitionKey

  object thriftSet extends SetColumn[ThriftTest]

  object thriftList extends ListColumn[ThriftTest]

  object thriftMap extends MapColumn[String, ThriftTest]

  object optionalThrift extends OptionalCol[ThriftTest]
}

abstract class ThriftDatabase(
  override val connector: CassandraConnection
) extends Database[ThriftDatabase](connector) {
  object thriftColumnTable extends ThriftColumnTable with Connector
  object thriftIndexedTable extends ThriftIndexedTable with Connector
}

object ThriftDatabase extends ThriftDatabase(Connector.default)