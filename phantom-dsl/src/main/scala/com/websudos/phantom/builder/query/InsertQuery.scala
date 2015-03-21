package com.websudos.phantom.builder.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](table: Table, val qb: CQLQuery) extends ExecutableStatement with Batchable {}

object InsertQuery {
  type Default[T <: CassandraTable[T, R], R] = InsertQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, R], R](table: T): InsertQuery.Default[T, R] = {
    new InsertQuery[T, R, Unspecified](table, QueryBuilder.insert(table.tableName))
  }
}


