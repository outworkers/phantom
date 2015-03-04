package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.UpdateClause
import UpdateClause.AssignmentCondition
import com.websudos.phantom.column.CounterColumn

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


  final def modify(clause: Table => UpdateClause.AssignmentCondition): UpdateQuery[Table, Record, Limit, Order, Status, Chain] = {
    new UpdateQuery(table, QueryBuilder.set(qb, clause(table).qb), row)
  }

}

private[phantom] trait UpdateImplicits {
  implicit class AssignmentsQuery[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](val query: UpdateQuery[T, R, L, O, S, C]) extends AnyVal {
  }

  implicit class CounterOperations[Owner <: CassandraTable[Owner, Record], Record](val col: CounterColumn[Owner, Record]) extends AnyVal {
    final def +=(value: Int = 1): UpdateClause.AssignmentCondition = {
      new AssignmentCondition(QueryBuilder.increment(col.name, value.toString))
    }

    final def -=(value: Int = 1): UpdateClause.AssignmentCondition = {
      new AssignmentCondition(QueryBuilder.decrement(col.name, value.toString))
    }

    final def increment = += _

    final def decrement = -= _


  }


}
