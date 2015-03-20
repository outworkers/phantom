package com.websudos.phantom.builder.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.{QueryBuilder, Unspecified, ConsistencyBound}


class AlterQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](table: CassandraTable[Table, Record], val qb: CQLQuery) extends ExecutableStatement {

}

object AlterQuery {

  type Default[T <: CassandraTable[T, R], R] = AlterQuery[T, R, Unspecified]

  /**
   * The root builder of an ALTER query.
   * This will initialise the alter builder chain and provide the initial {{{ ALTER $TABLENAME }}} query.
   * @param table The table to alter.
   * @tparam T The type of the table.
   * @tparam R The record held in the table.
   * @return A raw ALTER query, without any further options set on it.
   */
  def apply[T <: CassandraTable[T, _], R](table: CassandraTable[T, R]): AlterQuery[T, R, Unspecified] = {
    new AlterQuery[T, R, Unspecified](table, QueryBuilder.alter(table.tableName))
  }
}