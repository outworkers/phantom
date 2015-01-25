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


