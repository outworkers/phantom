package com.websudos.phantom.builder.primitives

import com.websudos.phantom.builder.query.CQLQuery

/**
 * A query that can be used inside "WHERE", "AND", and conditional compare-and-set type queries.
 *
 */
sealed abstract class QueryCondition(val qb: CQLQuery)

object WhereClause {
  class WhereCondition(override val qb: CQLQuery) extends QueryCondition(qb)
}

/**
 * Object enclosing a path dependant definition for CAS conditions.
 */
object ConditionalClause {

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
  class WhereCondition(override val qb: CQLQuery) extends QueryCondition(qb)
}

case class OrderingClause(qb: CQLQuery)
