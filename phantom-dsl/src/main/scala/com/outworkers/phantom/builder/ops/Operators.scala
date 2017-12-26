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
package com.outworkers.phantom.builder.ops

import java.util.Date

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.OperatorClause.Condition
import com.outworkers.phantom.builder.clauses.{OperatorClause, TypedClause, WhereClause}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.{AbstractColumn, Column, TimeUUIDColumn}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.{CassandraTable, TableAliases}
import org.joda.time.{DateTime, DateTimeZone}
import shapeless.{=:!=, HList}

sealed class CqlFunction extends SessionAugmenterImplicits

sealed class UnixTimestampOfCqlFunction extends CqlFunction {

  def apply[T <: CassandraTable[T, R], R](pf: TableAliases[T, R]#TimeUUIDColumn)(
    implicit ev: Primitive[Long],
    session: Session
  ): TypedClause.Condition[Option[Long]] = {
    new TypedClause.Condition(QueryBuilder.Select.unixTimestampOf(pf.name), row => {
      if (row.getColumnDefinitions.contains(s"system.unixtimestampof(${pf.name})")) {
        ev.fromRow(s"system.unixtimestampof(${pf.name})", row).toOption
      } else {
        ev.fromRow(s"unixtimestampof(${pf.name})", row).toOption
      }
    })
  }
}

sealed class TTLOfFunction extends CqlFunction {
  def apply(pf: Column[_, _, _])(implicit ev: Primitive[Int]): TypedClause.Condition[Option[Int]] = {
    new TypedClause.Condition(QueryBuilder.Select.ttl(pf.name), row => {
      ev.fromRow(s"ttl(${pf.name})", row).toOption
    })
  }
}

sealed class DateOfCqlFunction extends CqlFunction {

  protected[this] def apply(nm: String)(
    implicit ev: Primitive[DateTime],
    session: Session
  ): TypedClause.Condition[Option[DateTime]] = {
    new TypedClause.Condition(QueryBuilder.Select.dateOf(nm), row => {
      if (row.getColumnDefinitions.contains(s"system.dateof($nm)")) {
        ev.fromRow(s"system.dateof($nm)", row).toOption
      } else {
        ev.fromRow(s"dateof($nm)", row).toOption
      }
    })
  }

  def apply[T <: CassandraTable[T, R], R](pf: TimeUUIDColumn[T, R])(
    implicit ev: Primitive[DateTime],
    session: Session
  ): TypedClause.Condition[Option[DateTime]] = apply(pf.name)

  def apply(op: OperatorClause.Condition)(
    implicit ev: Primitive[DateTime],
    session: Session
  ): TypedClause.Condition[Option[DateTime]] = apply(op.qb.queryString)
}


sealed class AggregationFunction(operator: String) extends CqlFunction {
  protected[this] def apply[T](nm: String)(
    implicit ev: Primitive[T],
    numeric: Numeric[T],
    session: Session
  ): TypedClause.Condition[Option[T]] = {
    new TypedClause.Condition(QueryBuilder.Select.aggregation(operator, nm), row => {

      if (row.getColumnDefinitions.contains(s"system.$operator($nm)")) {
        ev.fromRow(s"system.$operator($nm)", row).toOption
      } else {
        ev.fromRow(s"$operator($nm)", row).toOption
      }
    })
  }

  def apply[T](pf: AbstractColumn[T])(
    implicit ev: Primitive[T],
    numeric: Numeric[T],
    session: Session
  ): TypedClause.Condition[Option[T]] = apply(pf.name)
}

sealed class CountCqlFunction extends CqlFunction {

  val operator = CQLSyntax.Selection.count
  val nm = CQLSyntax.Symbols.`*`

  def apply()(
    implicit ev: Primitive[Long],
    session: Session
  ): TypedClause.Condition[Option[Long]] = {
    new TypedClause.Condition(QueryBuilder.Select.aggregation(operator, nm), row => {

      if (row.getColumnDefinitions.contains(s"system.$operator($nm)")) {
        ev.fromRow(s"system.$operator", row).toOption
      } else {
        ev.fromRow(s"$operator", row).toOption
      }
    })
  }
}

sealed class SumCqlFunction extends AggregationFunction(CQLSyntax.Selection.sum)
sealed class AvgCqlFunction extends AggregationFunction(CQLSyntax.Selection.avg)
sealed class MinCqlFunction extends AggregationFunction(CQLSyntax.Selection.min)
sealed class MaxCqlFunction extends AggregationFunction(CQLSyntax.Selection.max)

sealed class NowCqlFunction extends CqlFunction {
  def apply()(implicit ev: Primitive[Long], session: Session): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.now())
  }
}

private[phantom] trait TimeUUIDOperator {
  def apply(date: Date): OperatorClause.Condition = apply(new DateTime(date, DateTimeZone.UTC))

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(fn(CQLQuery.escape(new DateTime(date).toString())))
  }

  def fn: String => CQLQuery
}

private[phantom] class MaxTimeUUID extends CqlFunction with TimeUUIDOperator {
  override def fn: (String) => CQLQuery = QueryBuilder.Select.maxTimeuuid
}


private[phantom] class MinTimeUUID extends CqlFunction with TimeUUIDOperator {
  override def fn: (String) => CQLQuery = QueryBuilder.Select.minTimeuuid
}

sealed class WritetimeCqlFunction extends CqlFunction {
  def apply(col: AbstractColumn[_])(implicit ev: Primitive[Long]): TypedClause.Condition[Long] = {
    val qb = QueryBuilder.Select.writetime(col.name)

    new TypedClause.Condition(qb, row =>
      ev.deserialize(row.getBytesUnsafe(qb.queryString), row.version)
    )
  }
}

sealed class TokenConstructor[P <: HList, TP <: TokenTypes.Root](val mapper: Seq[String]) {

  private[this] def joinOp(comp: Seq[String], op: String): WhereClause.Condition = {
    new WhereClause.Condition(
      QueryBuilder.Where.token(mapper: _*)
        .pad.append(op)
        .pad.append(QueryBuilder.Where.token(comp: _*))
    )
  }

  /**
    * An equals comparison clause between token definitions.
    *
    * @param tk The token constructor to compare against.
    * @tparam VL
    * @return
    */
  def eqs[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(
    implicit ev: TT =:!= TP
  ): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.eqs)
  }

  def <[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(
    implicit ev: TT =:!= TP
  ): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.lt)
  }

  def <=[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(
    implicit ev: TT =:!= TP
  ): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.lte)
  }

  def >[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(
    implicit ev: TT =:!= TP
  ): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.gt)
  }

  def >=[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(
    implicit ev: TT =:!= TP
  ): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.gte)
  }
}

sealed class TokenCqlFunction extends CqlFunction with TokenComparisonOps

trait Operators {
  object dateOf extends DateOfCqlFunction
  object unixTimestampOf extends UnixTimestampOfCqlFunction

  object minTimeuuid extends MinTimeUUID
  object maxTimeuuid extends MaxTimeUUID
  object token extends TokenCqlFunction
  object now extends NowCqlFunction
  object writetime extends WritetimeCqlFunction
  object ttl extends TTLOfFunction

  object count extends CountCqlFunction
  object sum extends SumCqlFunction
  object min extends MinCqlFunction
  object max extends MaxCqlFunction
  object avg extends AvgCqlFunction
}

