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

import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._

abstract class BasicTable extends CassandraTable[ConcreteBasicTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

abstract class ConcreteBasicTable extends BasicTable with RootConnector


object Records extends Enumeration {
  type Records = Value
  val TypeOne, TypeTwo, TypeThree = Value
}

object NamedRecords extends Enumeration {
  type NamedRecords = Value
  val One = Value("one")
  val Two = Value("two")
}

case class EnumRecord(
  name: String,
  enum: Records.type#Value,
  optEnum: Option[Records.type#Value]
)

case class NamedEnumRecord(
  name: String,
  enum: NamedRecords.type#Value,
  optEnum: Option[NamedRecords.type#Value]
)

abstract class EnumTable extends CassandraTable[ConcreteEnumTable, EnumRecord] {
  object id extends StringColumn(this) with PartitionKey[String]
  object enum extends EnumColumn[ConcreteEnumTable, EnumRecord, Records.type](this, Records)
  object optEnum extends OptionalEnumColumn[ConcreteEnumTable, EnumRecord, Records.type](this, Records)

  def fromRow(row: Row): EnumRecord = {
    EnumRecord(
      name = id(row),
      enum = enum(row),
      optEnum = optEnum(row)
    )
  }
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
  object id extends StringColumn(this) with PartitionKey[String]
  object enum extends EnumColumn[ConcreteNamedEnumTable, NamedEnumRecord, NamedRecords.type](this, NamedRecords)
  object optEnum extends OptionalEnumColumn[ConcreteNamedEnumTable, NamedEnumRecord, NamedRecords.type](this, NamedRecords)

  def fromRow(row: Row): NamedEnumRecord = {
    NamedEnumRecord(
      id(row),
      enum(row),
      optEnum(row)
    )
  }
}

abstract class ConcreteNamedEnumTable extends NamedEnumTable with PhantomCassandraConnector {
  def store(sample: NamedEnumRecord): InsertQuery.Default[ConcreteNamedEnumTable, NamedEnumRecord] = {
    insert
      .value(_.id, sample.name)
      .value(_.enum, sample.enum)
      .value(_.optEnum, sample.optEnum)
  }
}



sealed class ClusteringTable extends CassandraTable[ConcreteClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Ascending
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

abstract class ConcreteClusteringTable extends ClusteringTable with RootConnector

sealed class ComplexClusteringTable extends CassandraTable[ConcreteComplexClusteringTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with ClusteringOrder[UUID] with Ascending
  object id3 extends UUIDColumn(this) with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder[String] with Descending

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

abstract class ConcreteComplexClusteringTable extends ComplexClusteringTable with RootConnector


sealed class BrokenClusteringTable extends CassandraTable[ConcreteBrokenClusteringTable, String] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with ClusteringOrder[UUID] with Descending
  object placeholder extends StringColumn(this) with ClusteringOrder[String] with Descending

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

abstract class ConcreteBrokenClusteringTable extends BrokenClusteringTable


sealed class ComplexCompoundKeyTable extends CassandraTable[ConcreteComplexCompoundKeyTable, String] {

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

abstract class ConcreteComplexCompoundKeyTable extends ComplexCompoundKeyTable with RootConnector

sealed class SimpleCompoundKeyTable extends CassandraTable[ConcreteSimpleCompoundKeyTable, String] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object id2 extends UUIDColumn(this) with PrimaryKey[UUID]
  object id3 extends UUIDColumn(this) with PrimaryKey[UUID]
  object placeholder extends StringColumn(this)

  def fromRow(r: Row): String = {
    placeholder(r)
  }
}

abstract class ConcreteSimpleCompoundKeyTable extends SimpleCompoundKeyTable with RootConnector


