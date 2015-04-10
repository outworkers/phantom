package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.connectors.KeySpace

class DeleteQuery[
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
  ] = DeleteQuery[T, R, L, O, S, C]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](t: T, q: CQLQuery, r: Row => R): QueryType[T, R, L, O, S, C] = {
    new DeleteQuery[T, R, L, O, S, C](t, q)
  }
}

object DeleteQuery {

  type Default[T <: CassandraTable[T, _], R] = DeleteQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.delete(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

  def apply[T <: CassandraTable[T, _], R](table: T, col: String)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.deleteColumn(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString, col))
  }


}

