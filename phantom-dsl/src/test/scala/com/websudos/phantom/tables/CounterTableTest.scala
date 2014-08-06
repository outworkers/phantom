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
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.PhantomCassandraConnector
import com.websudos.phantom.helper.{ ModelSampler, TestSampler }
import com.websudos.phantom.Implicits._
import com.newzly.util.testing.Sampler

case class CounterRecord(id: UUID, count: Long)

object CounterRecord extends ModelSampler[CounterRecord] {
  def sample: CounterRecord = CounterRecord(
    UUIDs.timeBased,
    Sampler.getARandomInteger().toLong
  )
}

class CounterTableTest extends CassandraTable[CounterTableTest, CounterRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object count_entries extends CounterColumn(this)

  def fromRow(row: Row): CounterRecord = {
    CounterRecord(id(row), count_entries(row))
  }
}

object CounterTableTest extends CounterTableTest with TestSampler[CounterTableTest, CounterRecord] with PhantomCassandraConnector {
  override val tableName = "counter_column_tests"
}

class SecondaryCounterTable extends CassandraTable[SecondaryCounterTable, CounterRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object count_entries extends CounterColumn(this)

  def fromRow(row: Row): CounterRecord = {
    CounterRecord(id(row), count_entries(row))
  }
}

object SecondaryCounterTable extends SecondaryCounterTable with TestSampler[SecondaryCounterTable, CounterRecord]  with PhantomCassandraConnector {
  override val tableName = "secondary_column_tests"
}
