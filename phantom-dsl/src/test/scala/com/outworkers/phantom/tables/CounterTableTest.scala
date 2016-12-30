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

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.util.testing.sample
import com.outworkers.phantom.dsl._

case class CounterRecord(id: UUID, count: Long)

class CounterTableTest extends CassandraTable[ConcreteCounterTableTest, CounterRecord] {

  object id extends UUIDColumn(this) with PartitionKey
  object count_entries extends CounterColumn(this)
}

abstract class ConcreteCounterTableTest extends CounterTableTest with RootConnector {
  override val tableName = "counter_column_tests"
}

class SecondaryCounterTable extends CassandraTable[ConcreteSecondaryCounterTable, CounterRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object count_entries extends CounterColumn(this)
}

abstract class ConcreteSecondaryCounterTable extends SecondaryCounterTable with RootConnector {
  override val tableName = "secondary_column_tests"
}

class BrokenCounterTableTest extends CassandraTable[ConcreteBrokenCounterTableTest, CounterRecord] {

  object id extends UUIDColumn(this) with PartitionKey
  object count_entries extends CounterColumn(this)

}

abstract class ConcreteBrokenCounterTableTest extends BrokenCounterTableTest with RootConnector {
  override val tableName = "counter_column_tests"
}

