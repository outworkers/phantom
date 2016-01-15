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
package com.websudos.phantom.builder.ops

import java.util.{Date, UUID}

import com.datastax.driver.core.Row
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.clauses.OperatorClause.Condition
import com.websudos.phantom.builder.clauses.{OperatorClause, TypedClause, WhereClause}
import com.websudos.phantom.builder.primitives.{DefaultPrimitives, Primitive}
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.column.{AbstractColumn, TimeUUIDColumn}
import org.joda.time.DateTime
import shapeless.{=:!=, HList}

trait Extractors {
  protected[this] def timestamp(row: Row): Long = {
    row.getLong("timestamp")
  }
}

sealed class CqlFunction extends Extractors

sealed class DateOfCqlFunction extends CqlFunction {

  def apply[T <: CassandraTable[T, R], R](pf: TimeUUIDColumn[T, R]): TypedClause.Condition[Long] = {
    new TypedClause.Condition(QueryBuilder.Select.dateOf(pf.name), timestamp)
  }

  def apply(uuid: UUID): TypedClause.Condition[Long] = {
    new TypedClause.Condition(QueryBuilder.Select.dateOf(uuid.toString), timestamp)
  }

  def apply(op: OperatorClause.Condition): TypedClause.Condition[Long] = {
    new TypedClause.Condition(QueryBuilder.Select.dateOf(op.qb.queryString), timestamp)
  }
}

sealed class NowCqlFunction extends CqlFunction {
  def apply(): TypedClause.Condition[Long] = {
    new TypedClause.Condition(QueryBuilder.Select.now(), timestamp)
  }
}

sealed class MaxTimeUUID extends CqlFunction with DefaultPrimitives {

  private[this] val datePrimitive = implicitly[Primitive[Date]]
  private[this] val dateTimePrimitive = implicitly[Primitive[DateTime]]

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.maxTimeuuid(UUIDPrimitive.asCql(UUIDs.endOf(date.getTime))))
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.maxTimeuuid(UUIDPrimitive.asCql(UUIDs.endOf(date.getMillis))))
  }
}

sealed class MinTimeUUID extends CqlFunction with DefaultPrimitives {

  private[this] val datePrimitive = implicitly[Primitive[Date]]
  private[this] val dateTimePrimitive = implicitly[Primitive[DateTime]]

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.minTimeuuid(
        UUIDPrimitive.asCql(UUIDs.startOf(date.getTime))
      )
    )
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.minTimeuuid(
        UUIDPrimitive.asCql(UUIDs.startOf(date.getMillis))
      )
    )
  }
}

sealed class WritetimeCqlFunction extends CqlFunction {
  def apply[RR](col: AbstractColumn[RR]): TypedClause.Condition[Long] = {
    new TypedClause.Condition(QueryBuilder.Select.writetime(col.name), timestamp)
  }
}

sealed class TokenConstructor[P <: HList, TP <: TokenTypes.Root](val mapper : Seq[String]) {

  type Out = P

  private[this] def joinOp(comp: Seq[String], op: String): WhereClause.Condition = {
    val qb = QueryBuilder.Where.token(mapper: _*)
      .pad
      .append(op)
      .pad
      .append(QueryBuilder.Where.token(comp: _*))

    new WhereClause.Condition(qb)
  }

  /**
    * An equals comparison clause between token definitions.
    * @param tk The token constructor to compare against.
    * @tparam VL
    * @return
    */
  def eqs[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(implicit ev: TT =:!= TP): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.eqs)
  }

  def <[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(implicit ev: TT =:!= TP): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.lt)
  }

  def <=[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(implicit ev: TT =:!= TP): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.lte)
  }

  def >[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(implicit ev: TT =:!= TP): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.gt)
  }

  def >=[VL <: HList, TT <: TokenTypes.Root](tk: TokenConstructor[VL, TT])(implicit ev: TT =:!= TP): WhereClause.Condition = {
    joinOp(tk.mapper, CQLSyntax.Operators.gte)
  }
}

sealed class TokenCqlFunction extends CqlFunction with TokenComparisonOps

trait Operators {
  object dateOf extends DateOfCqlFunction
  object minTimeuuid extends MinTimeUUID
  object maxTimeuuid extends MaxTimeUUID
  object token extends TokenCqlFunction
  object now extends NowCqlFunction
  object writetime extends WritetimeCqlFunction
}

