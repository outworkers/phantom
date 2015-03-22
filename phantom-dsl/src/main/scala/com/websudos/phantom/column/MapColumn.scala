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

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery

import scala.annotation.implicitNotFound
import scala.util.Try


sealed abstract class AbstractMapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V](table: CassandraTable[Owner, Record])
  extends Column[Owner, Record, Map[K, V]](table) with CollectionValueDefinition[V] {

  def keyToCType(v: K): String

  def keyFromCType(c: String): K

  override def apply(r: Row): Map[K, V] = {
    optional(r).getOrElse(Map.empty[K, V])
  }

  protected[this] def parseMap(value: String): Option[Map[String, String]] = {
    Try {
      value.replaceAll("{", "").replaceAll("}", "").split(",\\s*").map {
        item => {
          val keyVal = item.split(",\\s*")
          Tuple2(keyVal(0), keyVal(2))
        }
      }.toMap
    } toOption
  }
}

@implicitNotFound(msg = "Type ${K} and ${V} must be Cassandra primitives")
class MapColumn[Owner <: CassandraTable[Owner, Record], Record, K : Primitive, V : Primitive](table: CassandraTable[Owner, Record])
    extends AbstractMapColumn[Owner, Record, K, V](table) with PrimitiveCollectionValue[V] {

  val keyPrimitive = Primitive[K]

  override def keyToCType(v: K): String = keyPrimitive.asCql(v)

  override val valuePrimitive = Primitive[V]

  override val cassandraType = QueryBuilder.Collections.mapType(keyPrimitive.cassandraType, valuePrimitive.cassandraType)

  override def qb: CQLQuery = QueryBuilder.Collections.mapType(keyPrimitive.cassandraType, valuePrimitive.cassandraType)

  override def keyFromCType(c: String): K = keyPrimitive.fromString(c)

  override def asCql(v: Map[K, V]): String = QueryBuilder.Collections.serialize(v.map {
    case (a, b) => (Primitive[K].asCql(a), Primitive[V].asCql(b))
  }).queryString

  override def optional(r: Row): Option[Map[K, V]] = {
    if (r.isNull(name)) {
      Some(Map.empty[K, V])
    } else {
      parseMap(r.getString(name)) map {
        existing => existing map {
          case (a, b) => (keyPrimitive.fromString(a), valuePrimitive.fromString(b))
        }
      }
    }
  }
}
