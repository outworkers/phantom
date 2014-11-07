/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.twitter.scrooge.CompactThriftSerializer
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector
import com.websudos.phantom.thrift.{OptionalThriftColumn, ThriftColumn, ThriftListColumn, ThriftMapColumn, ThriftSetColumn, ThriftTest}

case class Output(
  id: UUID,
  name: String,
  struct: ThriftTest,
  list: Set[ThriftTest],
  thriftList: List[ThriftTest],
  thriftMap: Map[String, ThriftTest],
  optThrift: Option[ThriftTest]
)

sealed class ThriftColumnTable extends CassandraTable[ThriftColumnTable, Output] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object ref extends ThriftColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftSet extends ThriftSetColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftList extends ThriftListColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftMap extends ThriftMapColumn[ThriftColumnTable, Output, String, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object optionalThrift extends OptionalThriftColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  def fromRow(row: Row): Output = {
    Output(
      id(row),
      name(row),
      ref(row),
      thriftSet(row),
      thriftList(row),
      thriftMap(row),
      optionalThrift(row)
    )
  }
}

object ThriftColumnTable extends ThriftColumnTable with PhantomCassandraConnector {
  override val tableName = "thrift_column_table"
}
