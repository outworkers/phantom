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
package com.outworkers.phantom.builder.query.prepared

import java.nio.ByteBuffer

import com.datastax.driver.core.ProtocolVersion
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive

/**
  * A special type wrapper
  *
  * @tparam T
  */
trait ListValue[T] {
  def value: List[T]
}

object ListValue {

  def apply[T](objects: T*): ListValue[T] = apply(objects.toList)

  def empty[T]: ListValue[T] = apply(List.empty[T])

  def apply[T](list: List[T]): ListValue[T] = new ListValue[T] {
    override def value: List[T] = list
  }

  implicit def primitive[T](
    implicit ev: Primitive[T],
    strP: Primitive[String]
  ): Primitive[ListValue[T]] = {
    new Primitive[ListValue[T]] {

      override def asCql(value: ListValue[T]): String = {
        QueryBuilder.Utils.join(value.value.map(ev.asCql)).queryString
      }

      override def dataType: String = ev.dataType

      override def serialize(obj: ListValue[T], protocol: ProtocolVersion): ByteBuffer = {
        strP.serialize(asCql(obj), protocol)
      }

      override def deserialize(source: ByteBuffer, protocol: ProtocolVersion): ListValue[T] = {
        ListValue.empty[T]
      }
    }
  }
}


