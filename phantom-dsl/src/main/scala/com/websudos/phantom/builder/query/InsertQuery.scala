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
package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.query.prepared.PrepareMark
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime
import shapeless.ops.hlist.Reverse
import shapeless.{::, Generic, HList, HNil}

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  PS <: HList
](
  table: Table,
  val init: CQLQuery,
  columnsPart: ColumnsPart = ColumnsPart.empty,
  valuePart: ValuePart = ValuePart.empty,
  usingPart: UsingPart = UsingPart.empty,
  lightweightPart: LightweightPart = LightweightPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None,
  override val parameters: Seq[Any] = Seq.empty
) extends ExecutableStatement with Batchable {

  final def value[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart append CQLQuery(col(table).name),
      valuePart append CQLQuery(col(table).asCql(value)),
      usingPart,
      lightweightPart,
      consistencyLevel,
      parameters
    )
  }

  def p_value[RR](col: Table => AbstractColumn[RR], value: PrepareMark) : InsertQuery[Table, Record, Status, RR :: PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart append CQLQuery(col(table).name),
      valuePart append value.qb,
      usingPart,
      lightweightPart,
      consistencyLevel,
      parameters
    )
  }

  def bind[V1, VL1 <: HList, Reversed <: HList](v1: V1)(
    implicit rev: Reverse.Aux[PS, Reversed],
    gen: Generic.Aux[V1, VL1],
    ev: VL1 =:= Reversed
  ) : InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart,
      lightweightPart,
      consistencyLevel = consistencyLevel,
      parameters = Seq(v1)
    )
  }


  final def valueOrNull[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status, PS] = {
    val insertValue = if (value != null) col(table).asCql(value) else null.asInstanceOf[String]

    new InsertQuery(
      table,
      init,
      columnsPart append CQLQuery(col(table).name),
      valuePart append CQLQuery(insertValue),
      usingPart,
      lightweightPart,
      consistencyLevel,
      parameters
    )
  }

  override def qb: CQLQuery = {
    (columnsPart merge valuePart merge usingPart merge lightweightPart) build init
  }

  def ttl(seconds: Long): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.ttl(seconds.toString),
      lightweightPart,
      consistencyLevel,
      parameters
    )
  }

  def ttl(seconds: scala.concurrent.duration.FiniteDuration): InsertQuery[Table, Record, Status, PS] = {
    ttl(seconds.toSeconds)
  }

  def ttl(duration: com.twitter.util.Duration): InsertQuery[Table, Record, Status, PS] = {
    ttl(duration.inSeconds)
  }

  final def timestamp(value: Long): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.timestamp(value.toString),
      lightweightPart,
      consistencyLevel,
      parameters
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): InsertQuery[Table, Record, Specified, PS] = {
    if (session.v3orNewer) {
      new InsertQuery(
        table,
        init,
        columnsPart,
        valuePart,
        usingPart,
        lightweightPart,
        Some(level),
        parameters
      )
    } else {
      new InsertQuery(
        table,
        init,
        columnsPart,
        valuePart,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        lightweightPart,
        None,
        parameters
      )
    }
  }

  final def timestamp(value: DateTime): InsertQuery[Table, Record, Status, PS] = {
    timestamp(value.getMillis)
  }

  def ifNotExists(): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart,
      lightweightPart append CQLQuery(CQLSyntax.ifNotExists),
      consistencyLevel,
      parameters
    )
  }
}

object InsertQuery {

  type Default[T <: CassandraTable[T, _], R] = InsertQuery[T, R, Unspecified, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = {
    new InsertQuery(
      table,
      QueryBuilder.Insert.insert(QueryBuilder.keyspace(keySpace.name, table.tableName))
    )
  }
}
