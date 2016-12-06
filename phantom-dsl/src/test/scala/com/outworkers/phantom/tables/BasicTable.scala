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

class BasicTable extends CassandraTable[ConcreteBasicTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with PrimaryKey
  object placeholder extends StringColumn(this)

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteBasicTable extends BasicTable with RootConnector


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

case class EnumRecord(
  name: String,
  enum: Records#Value,
  optEnum: Option[Records#Value]
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

abstract class EnumTable extends CassandraTable[ConcreteEnumTable, EnumRecord] {
  object id extends StringColumn(this) with PartitionKey
  object enum extends EnumColumn[Records#Value](this)
  object optEnum extends OptionalEnumColumn[Records#Value](this)
}

abstract class ConcreteEnumTable extends EnumTable with RootConnector {
  def store(sample: EnumRecord): InsertQuery.Default[ConcreteEnumTable, EnumRecord] = {
    insert
      .value(_.id, sample.name)
      .value(_.enum, sample.enum)
      .value(_.optEnum, sample.optEnum)
  }
}


sealed class NamedEnumTable extends CassandraTable[ConcreteNamedEnumTable, NamedEnumRecord] {
  object id extends StringColumn(this) with PartitionKey
  object enum extends EnumColumn[NamedRecords#Value](this)
  object optEnum extends OptionalEnumColumn[NamedRecords#Value](this)
}

abstract class ConcreteNamedEnumTable extends NamedEnumTable with RootConnector {
  def store(sample: NamedEnumRecord): InsertQuery.Default[ConcreteNamedEnumTable, NamedEnumRecord] = {
    insert
      .value(_.id, sample.name)
      .value(_.enum, sample.enum)
      .value(_.optEnum, sample.optEnum)
  }
}


sealed class NamedPartitionEnumTable extends CassandraTable[ConcreteNamedPartitionEnumTable, NamedPartitionRecord] {
  object enum extends EnumColumn[NamedRecords#Value](this) with PartitionKey
  object id extends UUIDColumn(this) with PrimaryKey
}

abstract class ConcreteNamedPartitionEnumTable extends NamedPartitionEnumTable with RootConnector {
  def store(sample: NamedPartitionRecord): InsertQuery.Default[ConcreteNamedPartitionEnumTable, NamedPartitionRecord] = {
    insert
      .value(_.id, sample.id)
      .value(_.enum, sample.enum)
  }
}

sealed class ClusteringTable extends CassandraTable[ConcreteClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with ClusteringOrder with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder with Descending
  object placeholder extends StringColumn(this)

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteClusteringTable extends ClusteringTable with RootConnector

sealed class ComplexClusteringTable extends CassandraTable[ConcreteComplexClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with ClusteringOrder with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder with Descending

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteComplexClusteringTable extends ComplexClusteringTable with RootConnector


sealed class BrokenClusteringTable extends CassandraTable[ConcreteBrokenClusteringTable, String] {
  object id extends UUIDColumn(this) with PartitionKey

  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with ClusteringOrder with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder with Descending

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteBrokenClusteringTable extends BrokenClusteringTable


sealed class ComplexCompoundKeyTable extends CassandraTable[ConcreteComplexCompoundKeyTable, String] {

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

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteComplexCompoundKeyTable extends ComplexCompoundKeyTable with RootConnector

sealed class SimpleCompoundKeyTable extends CassandraTable[ConcreteSimpleCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey
  object id2 extends UUIDColumn(this) with PrimaryKey
  object id3 extends UUIDColumn(this) with PrimaryKey
  object placeholder extends StringColumn(this)

  override def fromRow(r: Row): String = placeholder(r)
}

abstract class ConcreteSimpleCompoundKeyTable extends SimpleCompoundKeyTable with RootConnector


