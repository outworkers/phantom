package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.column.AbstractColumn



sealed trait Clause {
  /**
   * A query that can be used inside "WHERE", "AND", and conditional compare-and-set type queries.
   */
  sealed abstract class QueryCondition(val qb: CQLQuery)
}

object WhereClause extends Clause {

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
  class Condition(override val qb: CQLQuery) extends QueryCondition(qb)
}

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
  class Condition(override val qb: CQLQuery) extends QueryCondition(qb)
}

object OrderingClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition(qb)
}

object UpdateClause extends Clause {
  class Condition(override val qb: CQLQuery) extends QueryCondition(qb)
}

private[phantom] class OrderingColumn[RR](col: AbstractColumn[RR]) {

  def asc: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.ascending(col.name))
  def ascending: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.ascending(col.name))
  def desc: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.descending(col.name))
  def descending: OrderingClause.Condition = new OrderingClause.Condition(QueryBuilder.Select.Ordering.descending(col.name))
}