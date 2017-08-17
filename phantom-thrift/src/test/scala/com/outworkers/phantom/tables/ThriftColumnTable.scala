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
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.thrift.models._
import com.outworkers.phantom.thrift._

case class ThriftRecord(
  id: UUID,
  name: String,
  struct: ThriftTest,
  thriftSet: Set[ThriftTest],
  thriftList: List[ThriftTest],
  thriftMap: Map[String, ThriftTest],
  optThrift: Option[ThriftTest]
)

abstract class ThriftColumnTable extends Table[ThriftColumnTable, ThriftRecord] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object ref extends ThriftColumn[ThriftColumnTable, ThriftRecord, ThriftTest](this)

  object thriftSet extends ThriftSetColumn[ThriftColumnTable, ThriftRecord, ThriftTest](this)

  object thriftList extends ThriftListColumn[ThriftColumnTable, ThriftRecord, ThriftTest](this)

  object thriftMap extends ThriftMapColumn[ThriftColumnTable, ThriftRecord, String, ThriftTest](this)

  object optionalThrift extends OptionalThriftColumn[ThriftColumnTable, ThriftRecord, ThriftTest](this)
}

abstract class ThriftIndexedTable extends Table[ThriftIndexedTable, ThriftRecord] {

  object id extends UUIDColumn
  object name extends StringColumn

  object ref extends ThriftColumn[ThriftIndexedTable, ThriftRecord, ThriftTest](this) with PartitionKey

  object thriftSet extends ThriftSetColumn[ThriftIndexedTable, ThriftRecord, ThriftTest](this)

  object thriftList extends ThriftListColumn[ThriftIndexedTable, ThriftRecord, ThriftTest](this)

  object thriftMap extends ThriftMapColumn[ThriftIndexedTable, ThriftRecord, String, ThriftTest](this)

  object optionalThrift extends OptionalThriftColumn[ThriftIndexedTable, ThriftRecord, ThriftTest](this)
}

class ThriftDatabase(override val connector: CassandraConnection) extends Database[ThriftDatabase](connector) {
  object thriftColumnTable extends ThriftColumnTable with Connector
  object thriftIndexedTable extends ThriftIndexedTable with Connector
}

object ThriftDatabase extends ThriftDatabase(Connector.default)