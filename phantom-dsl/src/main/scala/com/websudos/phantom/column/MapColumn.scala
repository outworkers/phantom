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
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.ops.MapKeyUpdateClause
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

private[phantom] abstract class AbstractMapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V](table: CassandraTable[Owner, Record])
  extends Column[Owner, Record, Map[K, V]](table) with CollectionValueDefinition[V] {

  def keyAsCql(v: K): String

  def keyFromCql(c: String): K

  override def fromString(c: String): V

  def asCql(v: Map[K, V]): String = QueryBuilder.Collections.serialize(v.map {
    case (a, b) => (keyAsCql(a), valueAsCql(b))
  }).queryString

  override def apply(r: Row): Map[K, V] = {
    parse(r) match {
      case Success(map) => map

      // Note null rows will not result in a failure, we return an empty map for those.
      case Failure(ex) => {
        table.logger.error(ex.getMessage)
        throw ex
      }
    }
  }
}

@implicitNotFound(msg = "Type ${K} and ${V} must be Cassandra primitives")
class MapColumn[Owner <: CassandraTable[Owner, Record], Record, K : Primitive, V : Primitive](table: CassandraTable[Owner, Record])
    extends AbstractMapColumn[Owner, Record, K, V](table) with PrimitiveCollectionValue[V] {

  val keyPrimitive = Primitive[K]

  override def keyAsCql(v: K): String = keyPrimitive.asCql(v)

  override val valuePrimitive = Primitive[V]

  override val cassandraType = QueryBuilder.Collections.mapType(keyPrimitive.cassandraType, valuePrimitive.cassandraType).queryString

  override def qb: CQLQuery = {
    val base = CQLQuery(name).forcePad.append(cassandraType)

    if (shouldFreeze) {
      QueryBuilder.Collections.frozen(base)
    } else {
      base
    }
  }

  override def keyFromCql(c: String): K = keyPrimitive.fromString(c)

  override def valueAsCql(v: V): String = valuePrimitive.asCql(v)

  override def fromString(c: String): V = valuePrimitive.fromString(c)

  override def parse(r: Row): Try[Map[K, V]] = {
    if (r.isNull(name)) {
      Success(Map.empty[K, V])
    } else {
      Try(
        r.getMap(name, keyPrimitive.clz, valuePrimitive.clz).asScala.toMap map {
          case (k, v) => (keyPrimitive.extract(k), valuePrimitive.extract(v))
        }
      )
    }
  }

  def apply(k: K): MapKeyUpdateClause[K, V] = {
    new MapKeyUpdateClause[K, V](name, k)
  }
}
