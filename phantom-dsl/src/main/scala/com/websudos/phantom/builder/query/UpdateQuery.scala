package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.UpdateClause

class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table, qb: CQLQuery, row: Row => Record) extends Query[Table, Record, Limit, Order, Status, Chain](table, qb, row) with Batchable {

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ] = UpdateQuery[T, R, L, O, S, C]


  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](t: T, q: CQLQuery, r: Row => R): QueryType[T, R, L, O, S, C] = {
    new UpdateQuery[T, R, L, O, S, C](t, q, r)
  }


  final def modify(clause: Table => UpdateClause.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.set(qb, clause(table).qb), row)
  }

  final def and(clause: Table => UpdateClause.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.andSet(qb, clause(table).qb), row)
  }
}
