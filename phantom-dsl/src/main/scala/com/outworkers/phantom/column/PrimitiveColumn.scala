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

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[T <: CassandraTable[T, R], R, @specialized(Int, Double, Float, Long) RR : Primitive](t: CassandraTable[T, R])
  extends Column[T, R, RR](t) {

  def cassandraType: String = Primitive[RR].cassandraType

  def asCql(v: RR): String = Primitive[RR].asCql(v)

  def parse(r: Row): Try[RR] = implicitly[Primitive[RR]].fromRow(name, r)

  override def qb: CQLQuery = {
    val root = CQLQuery(name).forcePad.append(cassandraType)

    if (isStaticColumn) {
      root.forcePad.append(CQLSyntax.static)
    } else {
      root
    }
  }
}