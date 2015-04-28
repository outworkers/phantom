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
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.phantom.thrift._

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
