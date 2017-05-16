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

import com.outworkers.phantom.builder.syntax.CQLSyntax

import scala.annotation.implicitNotFound
import com.outworkers.phantom.{ CassandraTable, Row }
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.util.Try

@implicitNotFound(msg = "Type ${T} must be a Cassandra primitive")
class OptionalPrimitiveColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  @specialized(Int, Double, Float, Long, Boolean, Short) T
](table: CassandraTable[Owner, Record])(implicit ev: Primitive[T]) extends OptionalColumn[Owner, Record, T](table) {

  def cassandraType: String = ev.dataType

  def optional(r: Row): Try[T] = ev.fromRow(name, r)

  override def qb: CQLQuery = {
    val root = CQLQuery(name).forcePad.append(cassandraType)
    if (isStaticColumn) {
      root.forcePad.append(CQLSyntax.static)
    } else {
      root
    }
  }

  override def asCql(v: Option[T]): String = {
    v.map(Primitive[T].asCql).getOrElse(None.orNull.asInstanceOf[String])
  }
}
