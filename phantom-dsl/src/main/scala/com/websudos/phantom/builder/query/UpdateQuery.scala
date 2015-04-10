package com.websudos.phantom.builder.query

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.ops.{WhereClause, CompareAndSetClause, UpdateClause}
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound


class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table,
  init: CQLQuery,
  usingPart: UsingPart = Defaults.EmptyUsingPart,
  wherePart : WherePart = Defaults.EmptyWherePart,
  setPart : SetPart = Defaults.EmptySetPart
) extends Query[Table, Record, Limit, Order, Status, Chain](table, init, null) with Batchable {

  override val qb: CQLQuery = {
    usingPart merge setPart merge wherePart build init
  }

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

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  override def where(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Unchainned): UpdateQuery[Table, Record, Limit, Order, Status, Chainned] = {
    val query = QueryBuilder.Update.where(condition(table).qb)
    new UpdateQuery(table, init, usingPart, wherePart append query, setPart)
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @return A SelectCountWhere.
   */
  @implicitNotFound("You have to use an where clause before using an AND clause")
  override def and(condition: Table => WhereClause.Condition): UpdateQuery[Table, Record, Limit, Order, Status, Chainned] = {
    val query = QueryBuilder.Update.and(condition(table).qb)
    new UpdateQuery(table, init, usingPart, wherePart append query, setPart)
  }

  final def modify(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.Update.set(clause(table).qb)
    new AssignmentsQuery(table, init, usingPart, wherePart, setPart append query)
  }
}

sealed class AssignmentsQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table,
  val init: CQLQuery,
  usingPart: UsingPart = Defaults.EmptyUsingPart,
  wherePart : WherePart = Defaults.EmptyWherePart,
  setPart : SetPart = Defaults.EmptySetPart,
  casPart : CompareAndSetPart = Defaults.EmptyCompareAndSetPart
) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.Update.andSet(clause(table).qb)
    new AssignmentsQuery(table, init, usingPart, wherePart, setPart append query, casPart)
  }

  final def timestamp(value: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.using(QueryBuilder.timestamp(init, value.toString))
    new AssignmentsQuery(table, init, usingPart append query, wherePart, setPart, casPart)
  }

  /**
   * Generates a conditional query clause based on CQL lightweight transactions.
   * Compare and set transactions only get executed if a particular condition is true.
   *
   *
   * @param clause The Compare-And-Set clause to append to the builder.
   * @return A conditional query, now bound by a compare-and-set part.
   */
  def onlyIf(clause: Table => CompareAndSetClause.Condition): ConditionalQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.Update.onlyIf(qb, clause(table).qb)
    new ConditionalQuery(table, init, usingPart, wherePart, setPart, casPart append query)
  }
}

sealed class ConditionalQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table,
  val init: CQLQuery,
  usingPart: UsingPart = Defaults.EmptyUsingPart,
  wherePart : WherePart = Defaults.EmptyWherePart,
  setPart : SetPart = Defaults.EmptySetPart,
  casPart : CompareAndSetPart = Defaults.EmptyCompareAndSetPart
   ) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(clause: Table => CompareAndSetClause.Condition): ConditionalQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.Update.and(clause(table).qb)
    new ConditionalQuery(table, init, usingPart, wherePart, setPart, casPart append query)
  }

}

object UpdateQuery {

  type Default[T <: CassandraTable[T, _], R] = UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = {
    new UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned](
      table,
      QueryBuilder.update(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

}
