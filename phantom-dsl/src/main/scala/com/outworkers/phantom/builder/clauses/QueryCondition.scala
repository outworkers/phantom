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
package com.outworkers.phantom.builder.clauses

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.Row
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import shapeless.{::, HList, HNil}

abstract class QueryCondition[T <: HList](val qb: CQLQuery)

/**
  * A query that can be used inside "WHERE", "AND", and conditional compare-and-set type queries.
  */
sealed trait Clause

class PreparedCondition[RR] extends QueryCondition[RR :: HNil](PrepareMark.?.qb)
class ValueCondition[RR](val obj: RR) extends QueryCondition[HNil](CQLQuery.empty)

class WhereClause extends Clause {

  /**
   * A path dependant type condition used explicitly for WHERE clauses.
   * This is used to build and distinguish serialised queries that are used in primary index clauses.
   *
   * The columns that form the condition of the where clause are always part of the primary key.
   *
   * {{{
   *   SELECT WHERE id = 'test' LIMIT 1;
   *   UPDATE WHERE name = 'your_name' SET city = 'London';
   * }}}
   *
   * @param qb The underlying query builder of the condition.
   */
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)

  /**
   *
   * @tparam T Type of argument
   */
  class ParametricCondition[T](override val qb: CQLQuery) extends QueryCondition[T :: HNil](qb)

  class HListCondition[HL <: HList](override val qb: CQLQuery) extends QueryCondition[HL](qb)
}

object WhereClause extends WhereClause

/**
 * Object enclosing a path dependant definition for compare-and-set operations.
 */
object CompareAndSetClause extends Clause {

  /**
   * Using path dependent types to restrict builders from mixing up CAS queries with regular where queries.
   * Although the method names are similar, the behaviour of the two where clauses is fundamentally different.
   *
   * - A regular where clause can only be used by specifying the full primary key of the enclosing table.
   * - Any part of the WHERE .. AND .. chain must be part of the table's primary key.
   * - Conversely, any column used in a CAS condition must NOT be part of the primary key.
   *
   * An example of a CAS query:
   *
   * {{{
   *   UPDATE users WHERE id = 12412512 IF name = 'test';
   * }}}
   *
   * @param qb The underlying builder.
   */
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)
}

object OrderingClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)
}
object UsingClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)
}

object UpdateClause extends Clause {
  class Condition[HL <: HList](override val qb: CQLQuery, val skipped: Boolean = false) extends QueryCondition[HL](qb)

  type Default = Condition[HNil]

  type Prepared[RR] = Condition[RR :: HNil]
}

object OperatorClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)
}

object TypedClause extends Clause {
  class Condition[RR](override val qb: CQLQuery, val extractor: Row => RR) extends QueryCondition(qb)
}

object DeleteClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition[HNil](qb)
}

private[phantom] class OrderingColumn[RR](col: AbstractColumn[RR]) {

  def asc: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.ascending(col.name))
  def ascending: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.ascending(col.name))
  def desc: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.descending(col.name))
  def descending: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.descending(col.name))
}

trait UsingClauseOperations {
  object ignoreNulls extends UsingClause.Condition(CQLQuery(CQLSyntax.ignoreNulls))
}