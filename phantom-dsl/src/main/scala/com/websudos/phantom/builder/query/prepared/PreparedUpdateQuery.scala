package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.UpdateClause
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.connectors.KeySpace
import shapeless.{HList, HNil}

sealed class PreparedUpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  ReturnType <: HList
](table: Table, qb: CQLQuery) {

  //final def modify(clause: Table => UpdateClause.Condition): PreparedUpdateQuery[Table, Record, Limit, Order, Status, Chain, ReturnType] = {
    //new PreparedUpdateQuery(table, QueryBuilder.Update.set(qb, clause(table).qb))
  //}
}

object PreparedUpdateQuery {
  type Default[T <: CassandraTable[T, _], R] = PreparedUpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): PreparedUpdateQuery.Default[T, R] = {
    new PreparedUpdateQuery(table, QueryBuilder.insert(QueryBuilder.keyspace(keySpace.name, table.tableName)))
  }

}