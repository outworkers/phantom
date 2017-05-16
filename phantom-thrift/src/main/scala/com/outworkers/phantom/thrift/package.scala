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
package com.outworkers.phantom

import com.outworkers.phantom.builder.primitives.Primitive

package object thrift {
  type ThriftStruct = com.twitter.scrooge.ThriftStruct

  type ThriftColumn[
    T <: CassandraTable[T, R],
    R,
    Model <: ThriftStruct
  ] = columns.ThriftColumn[T, R, Model]

  type ThriftSetColumn[
    T <: CassandraTable[T, R],
    R,
    Model <: ThriftStruct
  ] = columns.ThriftSetColumn[T, R, Model]

  type ThriftListColumn[
    T <: CassandraTable[T, R],
    R,
    Model <: ThriftStruct
  ] = columns.ThriftListColumn[T, R, Model]

  type ThriftMapColumn[
    T <: CassandraTable[T, R],
    R,
    KeyType,
    Model <: ThriftStruct
  ] = columns.ThriftMapColumn[T, R, KeyType, Model]

  type OptionalThriftColumn[
    T <: CassandraTable[T, R],
    R,
    Model <: ThriftStruct
  ] = columns.OptionalThriftColumn[T, R, Model]

  implicit def thriftPrimitive[T <: ThriftStruct]()(implicit hp: ThriftHelper[T]): Primitive[T] = {
    val sz = hp.serializer
    Primitive.derive[T, String](sz.toString)(sz.fromString)
  }

  implicit class PrimitiveCompanionHelper(val obj: Primitive.type) extends AnyVal {
    def thrift[T <: ThriftStruct]()(implicit hp: ThriftHelper[T]): Primitive[T] = {
      val sz = hp.serializer
      Primitive.derive[T, String](sz.toString)(sz.fromString)
    }
  }
}



