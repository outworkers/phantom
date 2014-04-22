package com.newzly.phantom.column

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.newzly.phantom.query.QueryCondition

/**
 * A class enforcing columns used in where clauses to be indexed.
 * Using an implicit mechanism, only columns that are indexed can be converted into Indexed columns.
 * This enforces a Cassandra limitation at compile time.
 * It prevents a user from querying and using where operators on a column without any index.
 * @param col The column to cast to an IndexedColumn.
 * @tparam T The type of the value the column holds.
 */
class IndexedColumn[T](col: AbstractColumn[T]) {

  /**
   * The equals operator. Will return a match if the value equals the database value.
   * @param value The value to search for in the database.
   * @return A QueryCondition, wrapping a QueryBuilder clause.
   */
  def eqs(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.eq(col.name, col.toCType(value)))
  }

  def lt(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lt(col.name, col.toCType(value)))
  }

  def lte(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lte(col.name, col.toCType(value)))
  }

  def gt(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gt(col.name, col.toCType(value)))
  }

  def gte(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gte(col.name, col.toCType(value)))
  }

  def in(values: List[T]): QueryCondition = {
    QueryCondition(QueryBuilder.in(col.name, values.map(col.toCType): _*))
  }
}
