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
import scala.annotation.implicitNotFound
import org.joda.time.DateTime

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.CQLSyntax
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.CassandraTable

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[T <: CassandraTable[T, R], R, @specialized(Int, Double, Float, Long) RR: Primitive](t: CassandraTable[T, R])
  extends Column[T, R, RR](t) {

  def cassandraType: String = Primitive[RR].cassandraType

  def asCql(v: RR): String = Primitive[RR].asCql(v)

  def optional(r: Row): Option[RR] = implicitly[Primitive[RR]].fromRow(name, r)

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
