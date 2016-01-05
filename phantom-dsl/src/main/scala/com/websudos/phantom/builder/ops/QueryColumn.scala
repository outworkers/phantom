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

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.clauses.{UpdateClause, PreparedWhereClause, WhereClause, OperatorClause}
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.prepared.PrepareMark
import com.websudos.phantom.column.AbstractColumn

sealed class RequiredQueryColumn[RR : Primitive](col: AbstractColumn[RR]) extends QueryColumn(col)
sealed class OptionalQueryColumn[RR : Primitive](col: AbstractColumn[Option[RR]]) extends QueryColumn(col)

/**
 * A class enforcing columns used in where clauses to be indexed.
 * Using an implicit mechanism, only columns that are indexed can be converted into Indexed columns.
 * This enforces a Cassandra limitation at compile time.
 * It prevents a user from querying and using where operators on a column without any index.
 * @param col The column to cast to an IndexedColumn.
 * @tparam RR The type of the value the column holds.
 */
sealed abstract class QueryColumn[RR : Primitive](val col: AbstractColumn[_]) {

  private[this] val p = implicitly[Primitive[RR]]

  def eqs(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.eqs(col.name, p.asCql(value)))
  }

  def eqs(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.eqs(col.name, value.qb.queryString))
  }

  def lt(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(col.name, p.asCql(value)))
  }

  def <(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(col.name, p.asCql(value)))
  }

  def lt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(col.name, value.qb.queryString))
  }

  def <(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(col.name, value.qb.queryString))
  }

  def lte(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(col.name, implicitly[Primitive[RR]].asCql(value)))
  }

  def <=(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(col.name, implicitly[Primitive[RR]].asCql(value)))
  }

  def lte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(col.name, value.qb.queryString))
  }

  def <=(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(col.name, value.qb.queryString))
  }

  def gt(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(col.name, p.asCql(value)))
  }

  def >(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(col.name, p.asCql(value)))
  }

  def gt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(col.name, value.qb.queryString))
  }

  def >(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(col.name, value.qb.queryString))
  }

  def gte(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(col.name, p.asCql(value)))
  }

  def >=(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(col.name, p.asCql(value)))
  }

  def gte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(col.name, value.qb.queryString))
  }

  def >=(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(col.name, value.qb.queryString))
  }

  def in(values: List[RR]): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.in(col.name, values.map(p.asCql)))
  }

  /**
   * Equals clause defined for the prepared statement.
   * When this prepared clause is applied, the value specified in the WHERE clause can be binded at a later stage.
   *
   * {{{
   *   Example usage:
   *
   *   Table.select.where(_.id eqs ?)
   *
   *   Will produce
   *
   *   SELECT * FROM KEYSPACE.TABLE WHERE ID = ?
   *
   * }}}
   *
   *
   * @param value The prepare mark value to use, the "?" singleton.
   * @return A where clause with a parametric condition specified.
   */
  final def eqs(value: PrepareMark): PreparedWhereClause.ParametricCondition[RR] = {
    new PreparedWhereClause.ParametricCondition[RR](QueryBuilder.Where.eqs(col.name, value.symbol))
  }
}

class ColumnUpdateClause[K : Primitive, V : Primitive](val column: String, val key: K) {

  def keyName: String = Primitive[K].asCql(key)

  def setTo(v: V): UpdateClause.Condition = {
    val qb = QueryBuilder.Update.updateMapColumn(
      column,
      Primitive[K].asCql(key),
      Primitive[V].asCql(v)
    )

    new UpdateClause.Condition(qb)
  }
}