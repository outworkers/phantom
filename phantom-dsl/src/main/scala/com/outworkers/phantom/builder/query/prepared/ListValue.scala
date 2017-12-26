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

import com.outworkers.phantom.builder.primitives.Primitive

/**
  * A special type wrapper for binding to prepared in statement values.
  * This is required because Cassandra wants a special kind of protocol level input
  * for such bind values.
  *
  * @tparam T The underlying primitive type held here.
  */
trait ListValue[T] extends Serializable {
  def value: List[T]

  override def toString: String = value.toString()

  override def equals(obj: Any): Boolean = {
    obj match {
      case l: ListValue[T] => l.value == value
      case _ => false
    }
  }

  override def hashCode(): Int = value.hashCode()
}

object ListValue {

  def apply[T](objects: T*): ListValue[T] = apply(objects.toList)

  def empty[T]: ListValue[T] = apply(List.empty[T])

  def apply[T](list: List[T]): ListValue[T] = new ListValue[T] {
    override def value: List[T] = list
  }

  implicit def primitive[T](
    implicit ev: Primitive[List[T]]
  ): Primitive[ListValue[T]] = {
    Primitive.derive[ListValue[T], List[T]](_.value)(ListValue.apply)
  }
}


