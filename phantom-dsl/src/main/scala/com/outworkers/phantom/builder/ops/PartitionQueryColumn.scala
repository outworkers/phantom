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

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.{OperatorClause, WhereClause}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.prepared.{ListValue, PrepareMark}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits

/**
  * A class enforcing columns used in where clauses to be indexed.
  * Using an implicit mechanism, only columns that are indexed can be converted into Indexed columns.
  * This enforces a Cassandra limitation at compile time.
  * It prevents a user from querying and using where operators on a column without any index.
  * @param name The name of the column.
  * @tparam RR The type of the value the column holds.
  */
case class PartitionQueryColumn[RR](name: String)(
  implicit p: Primitive[RR]
) extends SessionAugmenterImplicits {

  protected[this] def operator[R : Primitive](
    value: R
  )(fn: (String, String) => CQLQuery)(implicit pp: Primitive[R]): WhereClause.PartitionCondition = {
    new WhereClause.PartitionCondition(
      fn(name, pp.asCql(value)), {
        session: Session => RoutingKeyValue(
          cql = pp.asCql(value),
          bytes = pp.serialize(value, session.protocolVersion)
        )
      }
    )
  }

  def eqs(value: RR): WhereClause.PartitionCondition = {
    operator(value)(QueryBuilder.Where.eqs)
  }

  def eqs(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.eqs(name, value.qb.queryString))
  }

  def lt(value: RR): WhereClause.PartitionCondition = {
    operator(value)(QueryBuilder.Where.lt)
  }

  def <(value: RR): WhereClause.PartitionCondition = lt(value)

  def lt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(name, value.qb.queryString))
  }

  def <(value: OperatorClause.Condition): WhereClause.Condition = lt(value)

  def lte(value: RR): WhereClause.PartitionCondition = {
    operator(value)(QueryBuilder.Where.lte)
  }

  def <=(value: RR): WhereClause.PartitionCondition = lte(value)

  def lte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(name, value.qb.queryString))
  }

  def <=(value: OperatorClause.Condition): WhereClause.Condition = lte(value)

  def gt(value: RR): WhereClause.PartitionCondition = {
    operator(value)(QueryBuilder.Where.gt)
  }

  def >(value: RR): WhereClause.PartitionCondition = gt(value)

  def gt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(name, value.qb.queryString))
  }

  def >(value: OperatorClause.Condition): WhereClause.Condition = gt(value)

  def gte(value: RR): WhereClause.PartitionCondition = {
    operator(value)(QueryBuilder.Where.gte)
  }

  def >=(value: RR): WhereClause.PartitionCondition = gte(value)

  def gte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(name, value.qb.queryString))
  }

  def >=(value: OperatorClause.Condition): WhereClause.Condition = gte(value)

  def in(values: List[RR])(
    implicit ev: Primitive[ListValue[RR]]
  ): WhereClause.PartitionCondition = {
    new WhereClause.PartitionCondition(
      QueryBuilder.Where.in(name, values.map(p.asCql)), {
        session: Session => RoutingKeyValue(
          s"List(${QueryBuilder.Utils.join(values.map(p.asCql)).queryString}",
          ev.serialize(ListValue(values), session.protocolVersion)
        )
      }
    )
  }

  final def in(value: PrepareMark): WhereClause.ParametricCondition[ListValue[RR]] = {
    new WhereClause.ParametricCondition[ListValue[RR]](QueryBuilder.Where.in(name, value))
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
  final def eqs(value: PrepareMark): WhereClause.ParametricCondition[RR] = {
    new WhereClause.ParametricCondition[RR](QueryBuilder.Where.eqs(name, value.symbol))
  }

  final def lt(value: PrepareMark): WhereClause.ParametricCondition[RR] = {
    new WhereClause.ParametricCondition[RR](QueryBuilder.Where.lt(name, value.symbol))
  }

  final def <(value: PrepareMark): WhereClause.ParametricCondition[RR] = lt(value)

  final def lte(value: PrepareMark): WhereClause.ParametricCondition[RR] = {
    new WhereClause.ParametricCondition[RR](QueryBuilder.Where.lte(name, value.symbol))
  }

  final def <=(value: PrepareMark): WhereClause.ParametricCondition[RR] = lte(value)

  final def gt(value: PrepareMark): WhereClause.ParametricCondition[RR] = {
    new WhereClause.ParametricCondition[RR](QueryBuilder.Where.gt(name, value.symbol))
  }

  final def >(value: PrepareMark): WhereClause.ParametricCondition[RR] = gt(value)

  final def gte(value: PrepareMark): WhereClause.ParametricCondition[RR] = {
    new WhereClause.ParametricCondition[RR](QueryBuilder.Where.gte(name, value.symbol))
  }

  final def >=(value: PrepareMark): WhereClause.ParametricCondition[RR] = gte(value)
}
