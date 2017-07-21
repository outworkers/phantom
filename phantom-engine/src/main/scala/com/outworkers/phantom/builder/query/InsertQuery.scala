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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.clauses._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatement}
import com.outworkers.phantom.builder.query.prepared.{PrepareMark, PreparedBlock}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import org.joda.time.DateTime
import shapeless.ops.hlist.Reverse
import shapeless.{::, =:!=, HList, HNil}

import scala.concurrent.{ExecutionContextExecutor, Future}

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  PS <: HList
](
  private[this] val table: Table,
  private[this] val init: CQLQuery,
  private[this] val columnsPart: ColumnsPart = ColumnsPart.empty,
  private[this] val valuePart: ValuePart = ValuePart.empty,
  private[this] val usingPart: UsingPart = UsingPart.empty,
  private[this] val lightweightPart: LightweightPart = LightweightPart.empty,
  options: QueryOptions = QueryOptions.empty
) extends Batchable {

  final def json(value: String): InsertJsonQuery[Table, Record, Status, PS] = {
    new InsertJsonQuery(
      table = table,
      init = QueryBuilder.Insert.json(init, value),
      usingPart = usingPart,
      lightweightPart = lightweightPart,
      options = options
    )
  }

  final def json(value: PrepareMark): InsertJsonQuery[Table, Record, Status, String :: PS] = {
    new InsertJsonQuery(
      table = table,
      init = QueryBuilder.Insert.json(init, value.qb.queryString),
      usingPart = usingPart,
      lightweightPart = lightweightPart,
      options = options
    )
  }

  /**
    * Insert function adding the ability to specify operator values as the value of an insert.
    * This is useful when we want to use functions to generate the CQL, such as using
    * the "now()" operator when inserting the value of a date.
    * @param col The function that selects a specific column from the table.
    * @param value The value to insert in the column, based on the output of the operator.
    * @return A new instance of insert query, with the clause added.
    */
  def valueOp(
    col: Table => AbstractColumn[_],
    value: OperatorClause.Condition
  ): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart append CQLQuery(col(table).name),
      valuePart append value.qb,
      usingPart,
      lightweightPart,
      options
    )
  }

  def values[RR](insertions: (CQLQuery, CQLQuery)*): InsertQuery[Table, Record, Status, PS] = {
    val (appendedCols, appendedVals) = (insertions :\ columnsPart -> valuePart) {
      case ((columnRef, valueRef), (cols, vals)) => Tuple2(cols append columnRef, vals append valueRef)
    }

    new InsertQuery(
      table,
      init,
      appendedCols,
      appendedVals,
      usingPart,
      lightweightPart,
      options
    )
  }

  def value[RR](
    col: Table => AbstractColumn[RR],
    value: RR
  )(): InsertQuery[Table, Record, Status, PS] = {
    copy(
      columnsPart = columnsPart append CQLQuery(col(table).name),
      valuePart = valuePart append CQLQuery(col(table).asCql(value))
    )
  }

  final def p_value[RR](
    col: Table => AbstractColumn[RR],
    value: PrepareMark
  ): InsertQuery[Table, Record, Status, RR :: PS] = {

    copy(
      columnsPart = columnsPart append CQLQuery(col(table).name),
      valuePart = valuePart append CQLQuery(col(table).asCql(value))
    )

    new InsertQuery(
      table,
      init,
      columnsPart append CQLQuery(col(table).name),
      valuePart append value.qb,
      usingPart,
      lightweightPart,
      options
    )
  }

  def prepare[Rev <: HList]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): PreparedBlock[Rev] = {
    val flatten = new PreparedFlattener(qb)
    new PreparedBlock(flatten.query, flatten.protocolVersion, options)
  }

  def prepareAsync[Rev <: HList]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): Future[PreparedBlock[Rev]] = {
    val flatten = new PreparedFlattener(qb)

    flatten.async map { ps =>
      new PreparedBlock(ps, flatten.protocolVersion, options)
    }
  }

  final def valueOrNull[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status, PS] = {
    if (Option(value).isDefined) {
      val insertValue = col(table).asCql(value)
      new InsertQuery(
        table,
        init,
        columnsPart append CQLQuery(col(table).name),
        valuePart append CQLQuery(insertValue),
        usingPart,
        lightweightPart,
        options
      )
    } else {
      this
    }
  }

  override def qb: CQLQuery = {
    (columnsPart merge valuePart merge lightweightPart merge usingPart) build init
  }

  final def ttl(value: PrepareMark): InsertQuery[Table, Record, Status, Int :: PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.ttl(value.qb.queryString),
      lightweightPart,
      options
    )
  }

  def ttl(seconds: Int): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.ttl(seconds.toString),
      lightweightPart,
      options
    )
  }

  def ttl(seconds: Long): InsertQuery[Table, Record, Status, PS] = ttl(seconds.toInt)

  def ttl(seconds: scala.concurrent.duration.FiniteDuration): InsertQuery[Table, Record, Status, PS] = {
    ttl(seconds.toSeconds)
  }

  def using(clause: UsingClause.Condition): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append clause.qb,
      lightweightPart,
      options
    )
  }

  final def timestamp(value: Long): InsertQuery[Table, Record, Status, PS] = {
    new InsertQuery(
      table,
      init,
      columnsPart,
      valuePart,
      usingPart append QueryBuilder.timestamp(value),
      lightweightPart,
      options
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): InsertQuery[Table, Record, Specified, PS] = {
    if (session.protocolConsistency) {
      new InsertQuery(
        table,
        init,
        columnsPart,
        valuePart,
        usingPart,
        lightweightPart,
        options.consistencyLevel_=(level)
      )
    } else {
      new InsertQuery(
        table,
        init,
        columnsPart,
        valuePart,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        lightweightPart,
        options
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
      options
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

class InsertJsonQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  PS <: HList
](
  table: Table,
  val init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  lightweightPart: LightweightPart = LightweightPart.empty,
  override val options: QueryOptions
) extends ExecutableStatement with Batchable {

  def prepare[Rev <: HList]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): PreparedBlock[Rev] = {
    val flatten = new PreparedFlattener(qb)
    new PreparedBlock(flatten.query, flatten.protocolVersion, options)
  }

  def prepareAsync[Rev <: HList]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): Future[PreparedBlock[Rev]] = {
    val flatten = new PreparedFlattener(qb)

    flatten.async map { ps =>
      new PreparedBlock(ps, flatten.protocolVersion, options)
    }
  }

  override val qb: CQLQuery = (lightweightPart merge usingPart) build init
}


