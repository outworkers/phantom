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
