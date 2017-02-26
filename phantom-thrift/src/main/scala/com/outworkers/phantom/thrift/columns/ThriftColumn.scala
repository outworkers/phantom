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

trait ThriftColumnDefinition[ValueType <: ThriftStruct] {

  /**
   * The Thrift serializer to use.
   * This must always be overriden before usage.
   * It is a simple forwarding from a concrete Thrift Struct.
   */
  implicit def serializer: CompactThriftSerializer[ValueType]

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

trait CollectionThriftColumnDefinition[ValueType <: ThriftStruct] extends ThriftColumnDefinition[ValueType] with CollectionValueDefinition[ValueType] {

  def fromString(c: String): ValueType = serializer.fromString(c)
}


abstract class ThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends Column[T, R, ValueType](table) with ThriftColumnDefinition[ValueType] {

  val cassandraType = CQLSyntax.Types.Text

  def parse(r: Row): Try[ValueType] = {
    Try(serializer.fromString(r.getString(name)))
  }
}

abstract class OptionalThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends OptionalColumn[T, R, ValueType](table)
  with ThriftColumnDefinition[ValueType] {

  val cassandraType = CQLSyntax.Types.Text

  def asCql(v: Option[ValueType]): String = {
    v.map(item => CQLQuery.empty.singleQuote(serializer.toString(item))).orNull
  }

  def optional(r: Row): Try[ValueType] = {
    Try(serializer.fromString(r.getString(name)))
  }

}

abstract class ThriftSetColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
    extends AbstractSetColumn[T, R, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {

  override val cassandraType = QueryBuilder.Collections.setType(CQLSyntax.Types.Text).queryString

  override def asCql(v: Set[ValueType]): String = Utils.set(v.map(valueAsCql)).queryString

  override def parse(r: Row): Try[Set[ValueType]] = {
    if (r.isNull(name)) {
      Success(Set.empty[ValueType])
    } else {
      Success(r.getSet(name, Primitive[String].clz.asInstanceOf[Class[String]]).asScala.map(fromString).toSet[ValueType])
    }
  }
}


abstract class ThriftListColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
    extends AbstractListColumn[T, R, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {

  override val cassandraType = QueryBuilder.Collections.listType(CQLSyntax.Types.Text).queryString

  override def parse(r: Row): Try[List[ValueType]] = {
    if (r.isNull(name)) {
      Success(Nil)
    } else {
      Success(r.getList(name, Primitive[String].clz.asInstanceOf[Class[String]]).asScala.map(fromString).toList)
    }
  }
}

@implicitNotFound(msg = "Type ${KeyType} must be a Cassandra primitive")
abstract class ThriftMapColumn[T <: CassandraTable[T, R], R, KeyType : Primitive, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends AbstractMapColumn[T, R, KeyType, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {

  val keyPrimitive = Primitive[KeyType]

  override val cassandraType = QueryBuilder.Collections.mapType(keyPrimitive.cassandraType, CQLSyntax.Types.Text).queryString

  override def keyAsCql(v: KeyType): String = keyPrimitive.asCql(v)

  override def keyFromCql(c: String): KeyType = keyPrimitive.fromString(c)

  override def parse(r: Row): Try[Map[KeyType, ValueType]] = {
    if (r.isNull(name)) {
      Success(Map.empty[KeyType, ValueType])
    } else {
      Try(
        r.getMap(name, keyPrimitive.clz.asInstanceOf[Class[KeyType]],
          Primitive[String].clz.asInstanceOf[Class[String]]
        ).asScala.toMap map {
          case (k, v) => (k, serializer.fromString(v))
        }
      )
    }
  }
}

abstract class RootThriftPrimitive[T <: ThriftStruct] extends Primitive[T] {

  def serializer: ThriftStructSerializer[T]

  override type PrimitiveType = java.lang.String

  override def fromRow(column: String, row: GettableByNameData): Try[T] = nullCheck(column, row) {
    existing => serializer.fromString(row.getString(column))
  }

  override def fromRow(index: Int, row: GettableByIndexData): Try[T] = nullCheck(index, row) {
    existing => serializer.fromString(row.getString(index))
  }

  override def cassandraType: String = CQLSyntax.Types.Text

  override def fromString(value: String): T = serializer.fromString(value)

  override def asCql(value: T): String = Primitive[String].asCql(serializer.toString(value))

  override def clz: Class[java.lang.String] = classOf[java.lang.String]
}