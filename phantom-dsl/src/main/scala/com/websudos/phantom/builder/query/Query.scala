package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Row}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.WhereClause

import scala.annotation.implicitNotFound


abstract class Query[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table, val qb: CQLQuery, row: Row => Record) extends ExecutableStatement {

  protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ] <: Query[T, R, L, O, S, C]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](t: T, q: CQLQuery, r: Row => R): QueryType[T, R, L, O, S, C]

  final def consistencyLevel(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified): QueryType[Table, Record, Limit, Order, Specified, Chain] = {
    create[Table, Record, Limit, Order, Specified, Chain](table, QueryBuilder.consistencyLevel(qb, level.toString), row)
  }

  final def limit(limit: Int)(implicit ev: Limit =:= Unlimited): QueryType[Table, Record, Limited, Order, Status, Chain] = {
    create[Table, Record, Limited, Order, Status, Chain](table, QueryBuilder.limit(qb, limit), row)
  }

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  def where(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Unchainned): QueryType[Table, Record, Limit, Order, Status, Chainned] = {
    create[Table, Record, Limit, Order, Status, Chainned](table, QueryBuilder.Where.where(qb, condition(table).qb), row)
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @return A SelectCountWhere.
   */
  @implicitNotFound("You have to use an where clause before using an AND clause")
  def and(condition: Table => WhereClause.Condition): QueryType[Table, Record, Limit, Order, Status, Chainned] = {
    create[Table, Record, Limit, Order, Status, Chainned](table, QueryBuilder.Where.and(qb, condition(table).qb), row)
  }
}


private[phantom] trait Batchable
