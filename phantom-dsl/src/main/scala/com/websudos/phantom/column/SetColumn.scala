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

import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery

import scala.annotation.implicitNotFound


abstract class AbstractSetColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
  extends Column[Owner, Record, Set[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): Set[String] = values.map(valueAsCql).toSet

  override def apply(r: Row): Set[RR] = {
    optional(r).getOrElse(Set.empty[RR])
  }

  override def asCql(v: Set[RR]): String = QueryBuilder.Collections.serialize(valuesToCType(v)).queryString

  override def optional(r: Row): Option[Set[RR]] = {
    if (r.isNull(name)) {
      None
    } else {
      Some(r.getSet(name, classOf[String]).asScala.map(fromString).toSet[RR])
    }
  }
}


@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SetColumn[Owner <: CassandraTable[Owner, Record], Record, RR : Primitive](table: CassandraTable[Owner, Record])
    extends AbstractSetColumn[Owner, Record, RR](table) with PrimitiveCollectionValue[RR] {

  override val valuePrimitive = Primitive[RR]

  val cassandraType = QueryBuilder.Collections.setType(valuePrimitive.cassandraType).queryString

  override def qb: CQLQuery = CQLQuery(name).forcePad.append(cassandraType)

  override def valueAsCql(v: RR): String = Primitive[RR].asCql(v)

  override def fromString(c: String): RR = Primitive[RR].fromString(c)

}
