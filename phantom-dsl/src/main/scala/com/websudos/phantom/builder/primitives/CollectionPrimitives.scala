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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.builder.primitives

import com.datastax.driver.core.GettableData
import com.websudos.phantom.builder.QueryBuilder
import scala.collection.JavaConverters._

import scala.util.Try

class CollectionPrimitives {

  def list[T : Primitive](
    name: String
  ): Primitive[List[T]] = {
    new Primitive[List[T]] {

      val ev = implicitly[Primitive[T]]

      override def fromRow(column: String, row: GettableData): Try[List[T]] = {
        Try(row.getList(name, ev.clz).asScala.toList.map(ev.extract))
      }

      override def cassandraType: String = QueryBuilder.Collections.listType(ev.cassandraType).queryString

      override def fromString(value: String): List[T] = value.split(",").map(Primitive[T].fromString).toList

      override def asCql(value: List[T]): String = QueryBuilder.Utils.collection(value.map(Primitive[T].asCql)).queryString

      override def clz: Class[List[Primitive[T]#PrimitiveType]] = classOf[List[Primitive[T]#PrimitiveType]]

      override type PrimitiveType = List[Primitive[T]#PrimitiveType]
    }
  }

  def set[T : Primitive](
    name: String
  ): Primitive[Set[T]] = {
    new Primitive[Set[T]] {

      val ev = implicitly[Primitive[T]]

      override def fromRow(column: String, row: GettableData): Try[Set[T]] = {
        Try(row.getSet(name, ev.clz).asScala.toSet.map(ev.extract))
      }

      override def cassandraType: String = QueryBuilder.Collections.setType(ev.cassandraType).queryString

      override def fromString(value: String): Set[T] = value.split(",").map(Primitive[T].fromString).toSet

      override def asCql(value: Set[T]): String = QueryBuilder.Utils.collection(value.map(Primitive[T].asCql)).queryString

      override def clz: Class[Set[Primitive[T]#PrimitiveType]] = classOf[Set[Primitive[T]#PrimitiveType]]

      override type PrimitiveType = Set[Primitive[T]#PrimitiveType]
    }
  }

  def map[K : Primitive, V : Primitive](
    name: String
  ): Primitive[Map[K, V]] = {
    new Primitive[Map[K, V]] {

      val keyPrimitive = implicitly[Primitive[K]]
      val valuePrimitive = implicitly[Primitive[V]]

      override def fromRow(column: String, row: GettableData): Try[Map[K, V]] = {
        Try {
          row.getMap(name, keyPrimitive.clz, valuePrimitive.clz).asScala.toMap.map {
            case (key, value) => keyPrimitive.extract(key) -> valuePrimitive.extract(value)
          }
        }
      }

      override def cassandraType: String = QueryBuilder.Collections.mapType(
        keyPrimitive.cassandraType,
        valuePrimitive.cassandraType
      ).queryString

      override def fromString(value: String): Map[K, V] = Map.empty[K, V]

      override def asCql(map: Map[K, V]): String = QueryBuilder.Utils.map(map.map {
        case (key, value) => Primitive[K].asCql(key) -> Primitive[V].asCql(value)
      }).queryString

      override def clz: Class[Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]] = {
        classOf[Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]]
      }

      override type PrimitiveType = Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]
    }
  }
}
