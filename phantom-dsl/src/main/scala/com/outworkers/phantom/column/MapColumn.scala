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
package com.outworkers.phantom.column

import com.outworkers.phantom.{ CassandraTable, Row }
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.ops.MapKeyUpdateClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.annotation.implicitNotFound
import scala.util.{Failure, Success, Try}

private[phantom] abstract class AbstractMapColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  K,
  V
](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Map[K, V]](table)
  with CollectionValueDefinition[V] {

  def keyAsCql(v: K): String

  def asCql(v: Map[K, V]): String = QueryBuilder.Collections.serialize(v.map {
    case (a, b) => keyAsCql(a) -> valueAsCql(b)
  }).queryString

  override def apply(r: Row): Map[K, V] = {
    parse(r) match {
      case Success(map) => map

      // Note null rows will not result in a failure, we return an empty map for those.
      case Failure(ex) =>
        table.logger.error(ex.getMessage)
        throw ex
    }
  }
}

@implicitNotFound(msg = "Type ${K} and ${V} must be Cassandra primitives")
class MapColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  K,
  V
](table: CassandraTable[Owner, Record])(
  implicit ev: Primitive[Map[K, V]],
  keyPrimitive: Primitive[K],
  val valuePrimitive: Primitive[V]
) extends AbstractMapColumn[Owner, Record, K, V](table) {

  override def keyAsCql(v: K): String = keyPrimitive.asCql(v)

  override val cassandraType: String = QueryBuilder.Collections.mapType(
    keyPrimitive,
    valuePrimitive
  ).queryString

  override def qb: CQLQuery = {
    if (shouldFreeze) {
      QueryBuilder.Collections.frozen(name, cassandraType)
    } else {
      CQLQuery(name).forcePad.append(cassandraType)
    }
  }

  override def valueAsCql(v: V): String = valuePrimitive.asCql(v)

  override def parse(r: Row): Try[Map[K, V]] = {
    if (r.isNull(name)) {
      Success(Map.empty[K, V])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name), r.version))
    }
  }

  def apply(k: K): MapKeyUpdateClause[K, V] = new MapKeyUpdateClause[K, V](name, k)
}
