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

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.clauses.OperatorClause.Condition
import com.websudos.phantom.builder.clauses.{OperatorClause, WhereClause}
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.column.TimeUUIDColumn
import org.joda.time.DateTime
import shapeless.{=:!=, HList}

sealed class Operator

sealed class DateOfOperator extends Operator {

  def apply[T <: CassandraTable[T, R], R](pf: TimeUUIDColumn[T, R]): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.dateOf(pf.name))
  }

  def apply(uuid: UUID): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.dateOf(uuid.toString))
  }

  def apply(op: OperatorClause.Condition): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.dateOf(op.qb.queryString))
  }
}

sealed class NowOperator extends Operator {
  def apply(): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.now())
  }
}

sealed class MaxTimeUUID extends Operator {

  private[this] val datePrimitive = implicitly[Primitive[Date]]
  private[this] val dateTimePrimitive = implicitly[Primitive[DateTime]]

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.maxTimeuuid(datePrimitive.asCql(date)))
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.maxTimeuuid(dateTimePrimitive.asCql(date)))
  }
}

sealed class MinTimeUUID extends Operator {

  private[this] val datePrimitive = implicitly[Primitive[Date]]
  private[this] val dateTimePrimitive = implicitly[Primitive[DateTime]]

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.minTimeuuid(datePrimitive.asCql(date)))
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(QueryBuilder.Select.minTimeuuid(dateTimePrimitive.asCql(date)))
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

sealed class TokenOperator extends Operator with TokenComparisonOps

trait Operators {
  object dateOf extends DateOfOperator
  object minTimeuuid extends MinTimeUUID
  object maxTimeuuid extends MaxTimeUUID
  object token extends TokenOperator
  object now extends NowOperator
}

