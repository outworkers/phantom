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
package com.websudos.phantom.column

import java.util.{ Map => JavaMap }
import scala.collection.breakOut
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable

abstract class AbstractMapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, Map[K, V]](table) with CollectionValueDefinition[V] {

  def keyCls: Class[_]

  def keyToCType(v: K): AnyRef

  def keyFromCType(c: AnyRef): K

  def valuesToCType(values: Traversable[(K, V)]): JavaMap[AnyRef, AnyRef] =
    values.map({ case (k, v) => keyToCType(k) -> valueToCType(v) }).toMap.asJava

  override def toCType(values: Map[K, V]): AnyRef = valuesToCType(values)

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty[K, V])
  }

  def optional(r: Row): Option[Map[K, V]] = {
    Option(r.getMap(name, keyCls, valueCls)).map(_.asScala.map {
      case (k, v) =>
        keyFromCType(k.asInstanceOf[AnyRef]) -> valueFromCType(v.asInstanceOf[AnyRef])
    }(breakOut) toMap)
  }
}
