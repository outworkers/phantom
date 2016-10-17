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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.outworkers.phantom.builder.ops

import java.util.Date

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.OperatorClause.Condition
import com.outworkers.phantom.builder.clauses.{OperatorClause, TypedClause, WhereClause}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.{AbstractColumn, Column, TimeUUIDColumn}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import org.joda.time.DateTime
import shapeless.{=:!=, HList}

sealed class CqlFunction extends SessionAugmenterImplicits

sealed class UnixTimestampOfCqlFunction extends CqlFunction {

  def apply(pf: TimeUUIDColumn[_, _])(
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

  def apply(pf: TimeUUIDColumn[_, _])(
    implicit ev: Primitive[DateTime],
    session: Session
  ): TypedClause.Condition[Option[DateTime]] = {
    new TypedClause.Condition(QueryBuilder.Select.dateOf(pf.name), row => {
      if (row.getColumnDefinitions.contains(s"system.dateof(${pf.name})")) {
        ev.fromRow(s"system.dateof(${pf.name})", row).toOption
      } else {
        ev.fromRow(s"dateof(${pf.name})", row).toOption
      }
    })
  }

  def apply(op: OperatorClause.Condition)(
    implicit ev: Primitive[DateTime],
    session: Session
  ): TypedClause.Condition[Option[DateTime]] = {
    val pf = op.qb.queryString

    new TypedClause.Condition(QueryBuilder.Select.dateOf(pf), row => {
      if (row.getColumnDefinitions.contains(s"system.dateof(${pf})")) {
        ev.fromRow(s"system.dateof($pf)", row).toOption
      } else {
        ev.fromRow(s"dateof($pf)", row).toOption
      }
    })
  }
}

sealed class NowCqlFunction extends CqlFunction {
  def apply()(implicit ev: Primitive[Long], session: Session): OperatorClause.Condition = {
    new OperatorClause.Condition(QueryBuilder.Select.now())
  }
}

sealed class MaxTimeUUID extends CqlFunction {

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.maxTimeuuid(
        CQLQuery.escape(new DateTime(date).toString())
      )
    )
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.maxTimeuuid(
        CQLQuery.escape(new DateTime(date).toString())
      )
    )
  }
}

sealed class MinTimeUUID extends CqlFunction {

  def apply(date: Date): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.minTimeuuid(
        CQLQuery.escape(new DateTime(date).toString())
      )
    )
  }

  def apply(date: DateTime): OperatorClause.Condition = {
    new Condition(
      QueryBuilder.Select.minTimeuuid(
        CQLQuery.escape(date.toString())
      )
    )
  }
}

sealed class WritetimeCqlFunction extends CqlFunction {
  def apply(col: AbstractColumn[_])(implicit ev: Primitive[BigDecimal]): TypedClause.Condition[Long] = {
    val qb = QueryBuilder.Select.writetime(col.name)

    new TypedClause.Condition(qb, row => {
      row.getLong(qb.queryString)
    })
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
    *
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
  object unixTimestampOf extends UnixTimestampOfCqlFunction

  object minTimeuuid extends MinTimeUUID
  object maxTimeuuid extends MaxTimeUUID
  object token extends TokenCqlFunction
  object now extends NowCqlFunction
  object writetime extends WritetimeCqlFunction
  object ttl extends TTLOfFunction
}

