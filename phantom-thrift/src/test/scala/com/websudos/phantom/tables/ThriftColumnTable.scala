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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.twitter.scrooge.CompactThriftSerializer
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.phantom.thrift._

case class Output(
  id: UUID,
  name: String,
  struct: ThriftTest,
  thriftSet: Set[ThriftTest],
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

  def store(sample: Output): InsertQuery.Default[ThriftColumnTable, Output] = {
    ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample.struct)
      .value(_.thriftSet, sample.thriftSet)
      .value(_.thriftList, sample.thriftList)
  }
}


sealed class ThriftIndexedTable extends CassandraTable[ThriftIndexedTable, Output] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  object ref extends ThriftColumn[ThriftIndexedTable, Output, ThriftTest](this) with PartitionKey[ThriftTest] {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftSet extends ThriftSetColumn[ThriftIndexedTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftList extends ThriftListColumn[ThriftIndexedTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object thriftMap extends ThriftMapColumn[ThriftIndexedTable, Output, String, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      val codec = ThriftTest
    }
  }

  object optionalThrift extends OptionalThriftColumn[ThriftIndexedTable, Output, ThriftTest](this) {
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

object ThriftIndexedTable extends ThriftIndexedTable with PhantomCassandraConnector {
  override val tableName = "thrift_indexed_table"

  def store(sample: Output): InsertQuery.Default[ThriftIndexedTable, Output] = {
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