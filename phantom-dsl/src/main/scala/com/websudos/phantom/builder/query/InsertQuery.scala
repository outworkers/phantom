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
import com.websudos.phantom.Manager
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime
import org.json4s.Formats

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](
  table: Table,
  val init: CQLQuery,
  jsonPart: JsonPart = Defaults.emptyJsonPart,
  columnsPart: ColumnsPart = Defaults.EmptyColumnsPart,
  valuePart: ValuePart = Defaults.EmptyValuePart,
  usingPart: UsingPart = Defaults.EmptyUsingPart,
  lightweightPart: LightweightPart = Defaults.EmptyLightweightPart,
  override val consistencyLevel: Option[ConsistencyLevel] = None
) extends ExecutableStatement with Batchable {

  final def json[RR](value: String) : InsertQuery[Table, Record, Status] = {
    new InsertQuery(
      table,
      init,
      jsonPart append QueryBuilder.json(value, table.formats),
      columnsPart,
      valuePart,
      usingPart,
      lightweightPart,
      consistencyLevel
    )
  }

  final def value[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status] = {
    new InsertQuery(
      table,
      init,
      jsonPart,
      columnsPart append CQLQuery.escapeDoubleQuotesQuery(col(table).name),
      valuePart append CQLQuery(col(table).asCql(value)),
      usingPart,
      lightweightPart,
      consistencyLevel
    )
  }

  final def valueOrNull[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status] = {
    val insertValue = if (value != null) col(table).asCql(value) else null.asInstanceOf[String]

    new InsertQuery(
      table,
      init,
      jsonPart,
      columnsPart append CQLQuery(col(table).name),
      valuePart append CQLQuery(insertValue),
      usingPart,
      lightweightPart,
      consistencyLevel
    )
  }

  override def qb: CQLQuery = {
    if ( jsonPart == Defaults.emptyJsonPart) {
      (columnsPart merge valuePart merge usingPart merge lightweightPart) build init
    }
    else {  // TODO: INSERT JSON queries may need using and lightweight clauses added
      (jsonPart) build init
    }
  }

  def ttl(seconds: Long): InsertQuery[Table, Record, Status] = {
    new InsertQuery(
      table,
      init,
      jsonPart,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.ttl(seconds.toString),
      lightweightPart,
      consistencyLevel
    )
  }

  def ttl(seconds: scala.concurrent.duration.FiniteDuration): InsertQuery[Table, Record, Status] = {
    ttl(seconds.toSeconds)
  }

  def ttl(duration: com.twitter.util.Duration): InsertQuery[Table, Record, Status] = {
    ttl(duration.inSeconds)
  }

  final def timestamp(value: Long): InsertQuery[Table, Record, Status] = {
    new InsertQuery(
      table,
      init,
      jsonPart,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.timestamp(value.toString),
      lightweightPart,
      consistencyLevel
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): InsertQuery[Table, Record, Specified] = {

    if (session.v3orNewer) {
      new InsertQuery(
        table,
        init,
        jsonPart,
        columnsPart,
        valuePart,
        usingPart,
        lightweightPart,
        Some(level)
      )
    } else {
      new InsertQuery(
        table,
        init,
        jsonPart,
        columnsPart,
        valuePart,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        lightweightPart
      )
    }
  }

  final def timestamp(value: DateTime): InsertQuery[Table, Record, Status] = {
    timestamp(value.getMillis)
  }

  def ifNotExists(): InsertQuery[Table, Record, Status] = {
    new InsertQuery(
      table,
      init,
      jsonPart,
      columnsPart,
      valuePart,
      usingPart,
      lightweightPart append CQLQuery(CQLSyntax.ifNotExists),
      consistencyLevel
    )
  }
}

object InsertQuery {

  type Default[T <: CassandraTable[T, _], R] = InsertQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = {
    new InsertQuery[T, R, Unspecified](
      table,
      QueryBuilder.Insert.insert(QueryBuilder.keyspace(keySpace.name, table.tableName))
    )
  }
}
