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
package com.websudos.phantom.thrift.columns

import com.datastax.driver.core.Row
import com.twitter.scrooge.{CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try
import com.websudos.phantom.column.{AbstractListColumn, AbstractMapColumn, AbstractSetColumn, CollectionValueDefinition, Column, OptionalColumn}
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}

import scala.annotation.implicitNotFound


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
  extends Column[T, R, ValueType](table) with ThriftColumnDefinition[ValueType] {

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
