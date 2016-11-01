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

/*
class Tuple2Column[Owner <: CassandraTable[Owner, Record], Record, K1 : Primitive, K2 : Primitive](table: Owner)
  extends Column[Owner, Record, (K1, K2)](table) {

  val p1 = implicitly[Primitive[K1]]
  val p2 = implicitly[Primitive[K2]]

  override def optional(r: Row): Option[(K1, K2)] = Option(r.getTupleValue(name)).map(value => {
    p1.fromRow(name, value.getTupleValue()
    p2.fromRow(name, value)
  }) match {
    case Tuple2(Success(tp1, tp2)) => Some(Tuple2(tp1, tp2))
    case _ => None
  }

  override def cassandraType: String = {
    QueryBuilder.Collections.tuple(
      name,
      implicitly[Primitive[K1]].cassandraType,
      implicitly[Primitive[K2]].cassandraType
    ).queryString
  }

  /**
   * Provides the serialisation mechanism of a value to a CQL string.
   * The vast majority of serializers are fed in via the Primitives mechanism.
   *
   * Primitive columns will automatically override and define "asCql" based on the
   * serialization of specific primitives. When T is context bounded by a primitive:
   *
   * {{{
   *   def asCql(v: T): String = implicitly[Primitive[T]].asCql(value)
   * }}}
   *
   * @param v The value of the object to convert to a string.
   * @return A string that can be directly appended to a CQL query.
   */
  override def asCql(v: (K1, K2)): String = {
    QueryBuilder.Collections
      .tupled(name, implicitly[Primitive[K1]].asCql(v._1), implicitly[Primitive[K2]].asCql(v._2))
      .queryString
  }
}*/