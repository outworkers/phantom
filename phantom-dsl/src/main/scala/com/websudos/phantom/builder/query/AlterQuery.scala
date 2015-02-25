package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.{ConsistencyBound, OrderBound, LimitBound, Query}


class AlterQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, qb: CQLQuery, row: Row => Record) extends Query[Table, Record, Limit, Order, Status](table, qb, row)
