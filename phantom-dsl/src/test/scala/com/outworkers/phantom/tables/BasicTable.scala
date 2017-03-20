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
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

abstract class BasicTable extends CassandraTable[BasicTable, String] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with PrimaryKey
  object placeholder extends StringColumn(this)

  override def fromRow(r: Row): String = placeholder(r)
}

trait Records extends Enumeration {
  type Records = Value
  val TypeOne, TypeTwo, TypeThree = Value
}

object Records extends Records

trait NamedRecords extends Enumeration {
  type NamedRecords = Value
  val One = Value("one")
  val Two = Value("two")
}

object NamedRecords extends NamedRecords

object SingletonEnum extends Enumeration {
  val One = Value("one")
  val Two = Value("two")
}

case class EnumRecord(
  name: String,
  enum: Records#Value,
  optEnum: Option[Records#Value],
  singleton: SingletonEnum.Value
)

case class NamedEnumRecord(
  name: String,
  enum: NamedRecords#Value,
  optEnum: Option[NamedRecords#Value]
)

case class NamedPartitionRecord(
  enum: NamedRecords#Value,
  id: UUID
)

abstract class EnumTable extends CassandraTable[EnumTable, EnumRecord] with RootConnector {
  object id extends StringColumn(this) with PartitionKey
  object enum extends EnumColumn[Records#Value](this)
  object optEnum extends OptionalEnumColumn[Records#Value](this)
  object singleton extends EnumColumn[SingletonEnum.Value](this)
}


abstract class NamedEnumTable extends CassandraTable[NamedEnumTable, NamedEnumRecord] with RootConnector {
  object id extends StringColumn(this) with PartitionKey
  object enum extends EnumColumn[NamedRecords#Value](this)
  object optEnum extends OptionalEnumColumn[NamedRecords#Value](this)
}

abstract class NamedPartitionEnumTable extends CassandraTable[
  NamedPartitionEnumTable,
  NamedPartitionRecord
] with RootConnector {
  object enum extends EnumColumn[NamedRecords#Value](this) with PartitionKey
  object id extends UUIDColumn(this) with PrimaryKey
}

abstract class ClusteringTable extends CassandraTable[ClusteringTable, String] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with ClusteringOrder with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder with Descending
  object placeholder extends StringColumn(this)
}

abstract class ComplexClusteringTable extends CassandraTable[ComplexClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with ClusteringOrder with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder with Descending
}

abstract class ComplexCompoundKeyTable extends CassandraTable[ComplexCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with PrimaryKey
  object id4 extends UUIDColumn(this) with PrimaryKey
  object id5 extends UUIDColumn(this) with PrimaryKey
  object id6 extends UUIDColumn(this) with PrimaryKey
  object id7 extends UUIDColumn(this) with PrimaryKey
  object id8 extends UUIDColumn(this) with PrimaryKey
  object id9 extends UUIDColumn(this) with PrimaryKey
  object placeholder extends StringColumn(this)
}

abstract class SimpleCompoundKeyTable extends CassandraTable[SimpleCompoundKeyTable, String] {
  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with PrimaryKey
  object placeholder extends StringColumn(this)
}


