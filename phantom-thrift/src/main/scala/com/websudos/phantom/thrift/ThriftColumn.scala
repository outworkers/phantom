/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.thrift

import scala.annotation.implicitNotFound
import com.datastax.driver.core.Row
import com.websudos.phantom.{ CassandraPrimitive, CassandraTable }
import com.websudos.phantom.column.{ AbstractListColumn, AbstractMapColumn, AbstractSetColumn, CollectionValueDefinition, Column, OptionalColumn }
import com.twitter.scrooge.{ CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try


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
   * @param v The Thrift struct to convert.
   * @return A string containing the compact Thrift serialization.
   */
  def itemToCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  val primitive = implicitly[CassandraPrimitive[String]]
}

trait CollectionThriftColumnDefinition[ValueType <: ThriftStruct] extends ThriftColumnDefinition[ValueType] with CollectionValueDefinition[ValueType] {

  override val valueCls: Class[_] = classOf[java.lang.String]

  override def valueToCType(v: ValueType): AnyRef = itemToCType(v)

  override def valueFromCType(c: AnyRef): ValueType = serializer.fromString(c.asInstanceOf[String])
}


abstract class ThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends Column[T, R, ValueType](table)
  with ThriftColumnDefinition[ValueType] {

  def toCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  val cassandraType = "text"

  def optional(r: Row): Option[ValueType] = {
    Try {
      serializer.fromString(r.getString(name))
    }.toOption
  }
}

abstract class OptionalThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends OptionalColumn[T, R, ValueType](table)
  with ThriftColumnDefinition[ValueType] {

  val cassandraType = "text"

  def toCType(v: Option[ValueType]): AnyRef = {
    v.map(serializer.toString).orNull
  }

  def optional(r: Row): Option[ValueType] = {
    Try {
      serializer.fromString(r.getString(name))
    }.toOption
  }

}

abstract class ThriftSetColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
    extends AbstractSetColumn[T, R, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {
  
  override val cassandraType = "set<text>"
}


abstract class ThriftListColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
    extends AbstractListColumn[T, R, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {

  override val cassandraType = "list<text>"
}

@implicitNotFound(msg = "Type ${KeyType} must be a Cassandra primitive")
abstract class ThriftMapColumn[T <: CassandraTable[T, R], R, KeyType : CassandraPrimitive, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends AbstractMapColumn[T, R, KeyType, ValueType](table) with CollectionThriftColumnDefinition[ValueType] {

  override val cassandraType = s"map<${CassandraPrimitive[KeyType].cassandraType}, text>"

  val keyPrimitive = CassandraPrimitive[KeyType]

  override def keyCls: Class[_] = keyPrimitive.cls

  override def keyToCType(v: KeyType): AnyRef = keyPrimitive.toCType(v)

  override def keyFromCType(c: AnyRef): KeyType = keyPrimitive.fromCType(c)
}
