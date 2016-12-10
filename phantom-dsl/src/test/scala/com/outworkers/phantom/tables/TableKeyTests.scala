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
import com.outworkers.phantom.dsl._

case class StubRecord(
  id: UUID,
  name: String
)

sealed class TableWithSingleKey extends CassandraTable[ConcreteTableWithSingleKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
}

abstract class ConcreteTableWithSingleKey extends TableWithSingleKey with RootConnector

class TableWithCompoundKey extends CassandraTable[ConcreteTableWithCompoundKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey
  object second extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)

  override def fromRow(r: Row): StubRecord = StubRecord(id(r), name(r))
}

abstract class ConcreteTableWithCompoundKey extends TableWithCompoundKey with RootConnector


sealed class TableWithCompositeKey extends CassandraTable[ConcreteTableWithCompositeKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey
  object second_part extends UUIDColumn(this) with PartitionKey
  object second extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)

  override def fromRow(r: Row): StubRecord = {
    StubRecord(
      id = id(r),
      name = name(r)
    )
  }
}

abstract class ConcreteTableWithCompositeKey extends TableWithCompositeKey with RootConnector


