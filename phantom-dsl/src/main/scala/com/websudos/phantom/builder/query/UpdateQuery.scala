package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.{CompareAndSet, UpdateClause}

class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table, qb: CQLQuery) extends Query[Table, Record, Limit, Order, Status, Chain](table, qb, null) with Batchable {

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
    new UpdateQuery[T, R, L, O, S, C](t, q)
  }


  final def onlyIf(clause: Table => CompareAndSet.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.Update.onlyIf(qb, clause(table).qb))
  }

  final def modify(clause: Table => UpdateClause.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.Update.set(qb, clause(table).qb))
  }

  final def andSet(clause: Table => UpdateClause.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.Update.andSet(qb, clause(table).qb))
  }
}

object UpdateQuery {

  type Default[T <: CassandraTable[T, _], R] = UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned]

  def apply[T <: CassandraTable[T, _], R](table: T): UpdateQuery.Default[T, R] = {
    new UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned](table, QueryBuilder.update(table.tableName))
  }
}
