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

import com.outworkers.phantom.dsl._

abstract class BasicTable extends Table[BasicTable, String] {

  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with PrimaryKey
  object id3 extends UUIDColumn with PrimaryKey
  object placeholder extends StringColumn

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

abstract class EnumTable extends Table[EnumTable, EnumRecord] {
  object id extends StringColumn with PartitionKey
  object enum extends EnumColumn[Records#Value]
  object optEnum extends OptionalEnumColumn[Records#Value]
  object singleton extends EnumColumn[SingletonEnum.Value]
}

abstract class NamedEnumTable extends Table[NamedEnumTable, NamedEnumRecord] {
  object id extends StringColumn with PartitionKey
  object enum extends EnumColumn[NamedRecords#Value]
  object optEnum extends OptionalEnumColumn[NamedRecords#Value]
}

abstract class NamedPartitionEnumTable extends Table[
  NamedPartitionEnumTable,
  NamedPartitionRecord
] {
  object enum extends EnumColumn[NamedRecords#Value] with PartitionKey
  object id extends UUIDColumn with PrimaryKey
}

abstract class ClusteringTable extends Table[ClusteringTable, String] {

  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with ClusteringOrder with Ascending
  object id3 extends UUIDColumn with ClusteringOrder with Descending
  object placeholder extends StringColumn
}

abstract class ComplexClusteringTable extends Table[ComplexClusteringTable, String] {

  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with ClusteringOrder with Ascending
  object id3 extends UUIDColumn with ClusteringOrder with Descending
  object placeholder extends StringColumn with ClusteringOrder with Descending
}

abstract class ComplexCompoundKeyTable extends Table[ComplexCompoundKeyTable, String] {
  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with PrimaryKey
  object id3 extends UUIDColumn with PrimaryKey
  object id4 extends UUIDColumn with PrimaryKey
  object id5 extends UUIDColumn with PrimaryKey
  object id6 extends UUIDColumn with PrimaryKey
  object id7 extends UUIDColumn with PrimaryKey
  object id8 extends UUIDColumn with PrimaryKey
  object id9 extends UUIDColumn with PrimaryKey
  object placeholder extends StringColumn
}

abstract class SimpleCompoundKeyTable extends Table[SimpleCompoundKeyTable, String] {
  object id extends UUIDColumn with PartitionKey
  object id2 extends UUIDColumn with PrimaryKey
  object id3 extends UUIDColumn with PrimaryKey
  object placeholder extends StringColumn
}
