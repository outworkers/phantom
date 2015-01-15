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
import com.websudos.phantom.PhantomCassandraConnector
import com.websudos.phantom.Implicits._

case class CounterRecord(id: UUID, count: Long)

class CounterTableTest extends CassandraTable[CounterTableTest, CounterRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object count_entries extends CounterColumn(this)

  def fromRow(row: Row): CounterRecord = {
    CounterRecord(id(row), count_entries(row))
  }
}

object CounterTableTest extends CounterTableTest with PhantomCassandraConnector {
  override val tableName = "counter_column_tests"
}

class SecondaryCounterTable extends CassandraTable[SecondaryCounterTable, CounterRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object count_entries extends CounterColumn(this)

  def fromRow(row: Row): CounterRecord = {
    CounterRecord(id(row), count_entries(row))
  }
}

object SecondaryCounterTable extends SecondaryCounterTable with PhantomCassandraConnector {
  override val tableName = "secondary_column_tests"
}
