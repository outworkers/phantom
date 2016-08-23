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
package com.websudos.phantom.builder.clauses

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.column.AbstractColumn
import shapeless.{HList, HNil, ::}

private[phantom] abstract class QueryCondition[T <: HList](val qb: CQLQuery)

sealed trait Clause {
  /**
   * A query that can be used inside "WHERE", "AND", and conditional compare-and-set type queries.
   */
}

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
  class ParametricCondition[T](override val qb: CQLQuery) extends QueryCondition(qb)
}

object WhereClause extends WhereClause

class PreparedWhereClause extends Clause {

  /**
   *
   * @tparam T Type of argument
   */
  class ParametricCondition[T](override val qb: CQLQuery) extends QueryCondition[T :: HNil](qb)
}

object PreparedWhereClause extends PreparedWhereClause

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
  class Condition(override val qb: CQLQuery, val skipped: Boolean = false) extends QueryCondition[HNil](qb)
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