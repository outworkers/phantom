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
package com.outworkers.phantom.thrift.columns

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import scala.util.{Success, Try}
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.datastax.driver.core.{GettableByIndexData, GettableByNameData, GettableData, Row}
import com.outworkers.phantom.CassandraTable
import com.twitter.scrooge.{CompactThriftSerializer, ThriftStruct, ThriftStructSerializer}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.column.{AbstractListColumn, AbstractMapColumn, AbstractSetColumn, CollectionValueDefinition, Column, OptionalColumn}
import com.outworkers.phantom.thrift.ThriftHelper

trait ThriftColumnDefinition[ValueType <: ThriftStruct] {

  /**
   * The Thrift serializer to use.
   * This must always be overriden before usage.
   * It is a simple forwarding from a concrete Thrift Struct.
   */
  def serializer: CompactThriftSerializer[ValueType]

  /**
   * This converts a value to the appropiate Cassandra type.
   * All Thrift structs are serialized to strings.
 *
   * @param v The Thrift struct to convert.
   * @return A string containing the compact Thrift serialization.
   */
  def asCql(v: ValueType): String = {
    CQLQuery.empty.singleQuote(serializer.toString(v))
  }

  def valueAsCql(v: ValueType): String = asCql(v)

  val primitive = implicitly[Primitive[String]]
}

trait CollectionThriftColumnDefinition[
  ValueType <: ThriftStruct
] extends ThriftColumnDefinition[ValueType] with CollectionValueDefinition[ValueType] {

  def fromString(c: String): ValueType = serializer.fromString(c)
}

abstract class ThriftColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V]
) extends Column[T, R, V](table) with ThriftColumnDefinition[V] {

  val cassandraType = CQLSyntax.Types.Text

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  def parse(r: Row): Try[V] = {
    Try(serializer.fromString(r.getString(name)))
  }
}

abstract class OptionalThriftColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V]
) extends OptionalColumn[T, R, V](table)
  with ThriftColumnDefinition[V] {

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  val cassandraType = CQLSyntax.Types.Text

  def asCql(v: Option[V]): String = {
    v.map(item => CQLQuery.empty.singleQuote(serializer.toString(item))).orNull
  }

  def optional(r: Row): Try[V] = {
    Try(serializer.fromString(r.getString(name)))
  }

}

abstract class ThriftSetColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  ev: Primitive[Set[String]]
) extends AbstractSetColumn[T, R, V](table) with CollectionThriftColumnDefinition[V] {

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override val cassandraType = QueryBuilder.Collections.setType(CQLSyntax.Types.Text).queryString

  override def asCql(v: Set[V]): String = Utils.set(v.map(valueAsCql)).queryString

  override def parse(r: Row): Try[Set[V]] = {
    if (r.isNull(name)) {
      Success(Set.empty[V])
    } else {
      Success(ev.deserialize(r.getBytesUnsafe(name)).map(fromString))
    }
  }
}


abstract class ThriftListColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  ev: Primitive[List[String]]
) extends AbstractListColumn[T, R, V](table) with CollectionThriftColumnDefinition[V] {

  override val cassandraType = QueryBuilder.Collections.listType(CQLSyntax.Types.Text).queryString

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override def parse(r: Row): Try[List[V]] = {
    if (r.isNull(name)) {
      Success(Nil)
    } else {
      Success(ev.deserialize(r.getBytesUnsafe(name)).map(fromString))
    }
  }
}

@implicitNotFound(msg = "Type ${KeyType} must be a Cassandra primitive")
abstract class ThriftMapColumn[
  T <: CassandraTable[T, R],
  R,
  KeyType : Primitive,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  val keyPrimitive: Primitive[KeyType],
  ev: Primitive[Map[KeyType, String]]
) extends AbstractMapColumn[T, R, KeyType, V](table) with CollectionThriftColumnDefinition[V] {

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override val cassandraType = QueryBuilder.Collections.mapType(
    keyPrimitive.cassandraType,
    CQLSyntax.Types.Text
  ).queryString

  override def keyAsCql(v: KeyType): String = keyPrimitive.asCql(v)

  override def keyFromCql(c: String): KeyType = keyPrimitive.fromString(c)

  override def parse(r: Row): Try[Map[KeyType, V]] = {
    if (r.isNull(name)) {
      Success(Map.empty[KeyType, V])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name)).mapValues(v => serializer.fromString(v)))
    }
  }
}