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

import java.util.Date

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import org.joda.time.{LocalDate, DateTime}

import scala.annotation.implicitNotFound
import scala.util.Try

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[T <: CassandraTable[T, R], R, @specialized(Int, Double, Float, Long) RR : Primitive](t: CassandraTable[T, R])
  extends Column[T, R, RR](t) {

  def cassandraType: String = Primitive[RR].cassandraType

  def asCql(v: RR): String = Primitive[RR].asCql(v)

  def optional(r: Row): Try[RR] = implicitly[Primitive[RR]].fromRow(name, r)

  override def qb: CQLQuery = {
    val root = CQLQuery(name).forcePad.append(cassandraType)

    if (isStaticColumn) {
      root.forcePad.append(CQLSyntax.static)
    } else {
      root
    }
  }
}

/**
 * A Date Column, used to enforce restrictions on clustering order.
 * @param table The Cassandra Table to which the column belongs to.
 * @tparam Owner The Owner of the Record.
 * @tparam Record The Record type.
 */
class DateColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record])
  extends PrimitiveColumn[Owner, Record, Date](table) {
}

/**
 * A DateTime Column, used to enforce restrictions on clustering order.
 * @param table The Cassandra Table to which the column belongs to.
 * @tparam Owner The Owner of the Record.
 * @tparam Record The Record type.
 */
class DateTimeColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record])
  extends PrimitiveColumn[Owner, Record, DateTime](table) {
}

/**
  * A LocalDate Column.
  * @param table The Cassandra Table to which the column belongs to.
  * @tparam Owner The Owner of the Record.
  * @tparam Record The Record type.
  */
class LocalDateColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record])
  extends PrimitiveColumn[Owner, Record, LocalDate](table) {
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