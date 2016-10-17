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
package com.outworkers.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.RootConnector
import com.twitter.scrooge.CompactThriftSerializer
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.connectors.KeySpaceDef
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._

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

  object id extends UUIDColumn(this) with PartitionKey[UUID]
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

  def fromRow(row: Row): Output = {
    Output(
      id = id(row),
      name = name(row),
      struct = ref(row),
      thriftSet = thriftSet(row),
      thriftList = thriftList(row),
      thriftMap = thriftMap(row),
      optThrift = optionalThrift(row)
    )
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

  object ref extends ThriftColumn[ConcreteThriftIndexedTable, Output, ThriftTest](this) with PartitionKey[ThriftTest] {
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

class ThriftDatabase(override val connector: KeySpaceDef) extends Database[ThriftDatabase](connector) {
  object thriftColumnTable extends ConcreteThriftColumnTable with connector.Connector
  object thriftIndexedTable extends ConcreteThriftIndexedTable with connector.Connector
}

object ThriftDatabase extends ThriftDatabase(connectors.ContactPoint.local.keySpace("phantom"))