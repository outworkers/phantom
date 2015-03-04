package com.websudos.phantom.builder.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, val qb: CQLQuery) extends ExecutableStatement {

}
