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

import com.outworkers.phantom.{CassandraTable, Row}
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.primitives.Primitives.StringPrimitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column._
import com.outworkers.phantom.thrift.ThriftHelper
import com.twitter.scrooge.{CompactThriftSerializer, ThriftStruct}

import scala.annotation.implicitNotFound
import scala.util.{Success, Try}

sealed trait ThriftCol[ValueType <: ThriftStruct] {

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

abstract class ThriftColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V]
) extends Column[T, R, V](table) with ThriftCol[V] {

  val cassandraType = CQLSyntax.Types.Text

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  def parse(r: Row): Try[V] = {
    Try(StringPrimitive.deserialize(r.getBytesUnsafe(name), r.version)) map serializer.fromString
  }
}

abstract class OptionalThriftColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V]
) extends OptionalColumn[T, R, V](table) with ThriftCol[V] {

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  val cassandraType = CQLSyntax.Types.Text

  def asCql(v: Option[V]): String = {
    v.map(item => CQLQuery.empty.singleQuote(serializer.toString(item))).orNull
  }

  def optional(r: Row): Try[V] = {
    Try(StringPrimitive.deserialize(r.getBytesUnsafe(name), r.version)) map serializer.fromString
  }

}

class ThriftSetColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  ev1: Primitive[String],
  ev: Primitive[Set[String]]
) extends AbstractColColumn[T, R, Set, V](table) with ThriftCol[V] {

  override def valueAsCql(v: V): String = ev1.asCql(serializer.toString(v))

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override val cassandraType = QueryBuilder.Collections.setType(CQLSyntax.Types.Text).queryString

  override def asCql(v: Set[V]): String = ev.asCql(v map serializer.toString)

  override def parse(r: Row): Try[Set[V]] = {
    if (r.isNull(name)) {
      Success(Set.empty[V])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name), r.version) map serializer.fromString)
    }
  }
}


class ThriftListColumn[
  T <: CassandraTable[T, R],
  R,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  ev: Primitive[List[String]]
) extends AbstractColColumn[T, R, List, V](table) with ThriftCol[V] {

  override def valueAsCql(v: V): String = {
    CQLQuery.empty.singleQuote(serializer.toString(v))
  }

  override val cassandraType = QueryBuilder.Collections.listType(CQLSyntax.Types.Text).queryString

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override def parse(r: Row): Try[List[V]] = {
    if (r.isNull(name)) {
      Success(Nil)
    } else {
      Success(ev.deserialize(r.getBytesUnsafe(name), r.version).map(serializer.fromString))
    }
  }

  /**
    * Provides the serialisation mechanism of a value to a CQL string.
    * The vast majority of serializers are fed in via the Primitives mechanism.
    *
    * Primitive columns will automatically override and define "asCql" based on the
    * serialization of specific primitives. When T is context bounded by a primitive:
    *
    * {{{
    *   def asCql(v: T): String = implicitly[Primitive[T]].asCql(value)
    * }}}
    *
    * @param v The value of the object to convert to a string.
    * @return A string that can be directly appended to a CQL query.
    */
  override def asCql(v: List[V]): String = ev.asCql(v map valueAsCql)
}

@implicitNotFound(msg = "Type ${KeyType} must be a Cassandra primitive")
class ThriftMapColumn[
  T <: CassandraTable[T, R],
  R,
  KeyType : Primitive,
  V <: ThriftStruct
](table: CassandraTable[T, R])(
  implicit hp: ThriftHelper[V],
  val keyPrimitive: Primitive[KeyType],
  ev: Primitive[Map[KeyType, String]]
) extends AbstractMapColumn[T, R, KeyType, V](table) with ThriftCol[V] {

  override val serializer: CompactThriftSerializer[V] = hp.serializer

  override val cassandraType = QueryBuilder.Collections.mapType(
    keyPrimitive.dataType,
    CQLSyntax.Types.Text
  ).queryString

  override def keyAsCql(v: KeyType): String = keyPrimitive.asCql(v)

  override def parse(r: Row): Try[Map[KeyType, V]] = {
    if (r.isNull(name)) {
      Success(Map.empty[KeyType, V])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name), r.version).mapValues(serializer.fromString))
    }
  }
}