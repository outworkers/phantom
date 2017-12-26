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

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses._
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.prepared.{ListValue, PrepareMark}

/**
 * A class enforcing columns used in where clauses to be indexed.
 * Using an implicit mechanism, only columns that are indexed can be converted into Indexed columns.
 * This enforces a Cassandra limitation at compile time.
 * It prevents a user from querying and using where operators on a column without any index.
 * @param name The name of the column.
 * @tparam RR The type of the value the column holds.
 */
abstract class RootQueryColumn[RR](val name: String)(implicit p: Primitive[RR]) {

  def eqs(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.eqs(name, p.asCql(value)))
  }


  def eqs(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.eqs(name, value.qb.queryString))
  }

  def lt(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(name, p.asCql(value)))
  }

  def <(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(name, p.asCql(value)))
  }

  def lt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(name, value.qb.queryString))
  }

  def <(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lt(name, value.qb.queryString))
  }

  def lte(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(name, implicitly[Primitive[RR]].asCql(value)))
  }

  def <=(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(name, implicitly[Primitive[RR]].asCql(value)))
  }

  def lte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(name, value.qb.queryString))
  }

  def <=(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.lte(name, value.qb.queryString))
  }

  def gt(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(name, p.asCql(value)))
  }

  def >(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(name, p.asCql(value)))
  }

  def gt(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(name, value.qb.queryString))
  }

  def >(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gt(name, value.qb.queryString))
  }

  def gte(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(name, p.asCql(value)))
  }

  def >=(value: RR): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(name, p.asCql(value)))
  }

  def gte(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(name, value.qb.queryString))
  }

  def >=(value: OperatorClause.Condition): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.gte(name, value.qb.queryString))
  }

  def in(values: List[RR]): WhereClause.Condition = {
    new WhereClause.Condition(QueryBuilder.Where.in(name, values.map(p.asCql)))
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

/**
  * Class used to provide serialization ability for updating specific keys of a map column.
  * This CQL syntax allows users to manipulate the content of a Cassandra map column.
  *
  * Example: {{{
  *   Database.table.update.where(_.id eqs id).modify(_.map(key) setTo value).future()
  * }}}
  *
  * @param column The name of the column to update, derived from MapColumn.apply.
  * @param key The type of the key required, strongly typed.
  * @tparam K The strong type of the key in the map.
  * @tparam V The strong type of the value in the map.
  */
class MapKeyUpdateClause[K : Primitive, V : Primitive](val column: String, val key: K) {

  def keyName: String = Primitive[K].asCql(key)

  def setTo(v: V): UpdateClause.Default = {
    val qb = QueryBuilder.Update.updateMapColumn(
      column,
      Primitive[K].asCql(key),
      Primitive[V].asCql(v)
    )

    new UpdateClause.Condition(qb)
  }

  /**
    * Overloaded variants of setTo that allows using prepared statements for map key updates.
    * This will only accept the ? global singleton found in [[com.outworkers.phantom.dsl]].
    * When used, the final "bind" to the prepared clause will require an additional V type
    * in the provided tuple to match the type of the MapColumn being updated.
    *
    * @param mark The value of the prepared mark used.
    * @return A parametric condition on the value type of the map.
    */
  final def setTo(mark: PrepareMark): WhereClause.ParametricCondition[V] = {
    new WhereClause.ParametricCondition[V](
      QueryBuilder.Update.updateMapColumn(
        column,
        Primitive[K].asCql(key),
        mark.symbol
      )
    )
  }
}

class QueryColumn[RR](override val name: String)(implicit pv: Primitive[RR]) extends RootQueryColumn[RR](name)
class DeleteQueryColumn[RR](override val name: String)(implicit pv: Primitive[RR]) extends RootQueryColumn[RR](name)