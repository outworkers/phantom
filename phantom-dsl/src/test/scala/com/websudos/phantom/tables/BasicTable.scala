/*
 * Copyright 2014 websudos ltd.
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

import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector

sealed class BasicTable extends CassandraTable[BasicTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object BasicTable extends BasicTable with PhantomCassandraConnector


object Records extends Enumeration {
  type Records = Value
  val TypeOne, TypeTwo, TypeThree = Value
}

case class EnumRecord(
  name: String,
  enum: Records.type#Value,
  optEnum: Option[Records.type#Value]
)

sealed class EnumTable extends CassandraTable[EnumTable, EnumRecord] {
  object id extends StringColumn(this) with PartitionKey[String]
  object enum extends EnumColumn[EnumTable, EnumRecord, Records.type](this, Records)
  object optEnum extends OptionalEnumColumn[EnumTable, EnumRecord, Records.type](this, Records)

  def fromRow(row: Row): EnumRecord = {
    EnumRecord(
      id(row),
      enum(row),
      optEnum(row)
    )
  }
}

object EnumTable extends EnumTable with PhantomCassandraConnector

sealed class ClusteringTable extends CassandraTable[ClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Ascending
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object ClusteringTable extends ClusteringTable with PhantomCassandraConnector

sealed class ComplexClusteringTable extends CassandraTable[ComplexClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with ClusteringOrder[UUID] with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder[String] with Descending

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object ComplexClusteringTable extends ComplexClusteringTable with PhantomCassandraConnector


sealed class BrokenClusteringTable extends CassandraTable[BrokenClusteringTable, String] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder[String] with Descending

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object BrokenClusteringTable extends BrokenClusteringTable


sealed class ComplexCompoundKeyTable extends CassandraTable[ComplexCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id4 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id5 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id6 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id7 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id8 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id9 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object ComplexCompoundKeyTable extends ComplexCompoundKeyTable with PhantomCassandraConnector

sealed class SimpleCompoundKeyTable extends CassandraTable[SimpleCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

object SimpleCompoundKeyTable extends SimpleCompoundKeyTable with PhantomCassandraConnector


