/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Row}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.clauses.{CompareAndSetClause, WhereClause}
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound

class DeleteQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: PSBound
](table: Table,
  init: CQLQuery,
  wherePart : WherePart = WherePart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None
) extends Query[Table, Record, Limit, Order, Status, Chain, PS](table, init, null) with Batchable {

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: PSBound
  ] = DeleteQuery[T, R, L, O, S, C, P]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: PSBound
  ](t: T, q: CQLQuery, r: Row => R, consistencyLevel: Option[ConsistencyLevel] = None): QueryType[T, R, L, O, S, C, P] = {
    new DeleteQuery[T, R, L, O, S, C, P](t, q, Defaults.EmptyWherePart, Defaults.EmptyCompareAndSetPart, consistencyLevel)
  }

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  override def where(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Unchainned): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    val query = QueryBuilder.Update.where(condition(table).qb)
    new DeleteQuery(table, init, wherePart append query, casPart, consistencyLevel)
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @return A SelectCountWhere.
   */
  @implicitNotFound("You have to use an where clause before using an AND clause")
  override def and(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Chainned): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    val query = QueryBuilder.Update.and(condition(table).qb)
    new DeleteQuery(table, init, wherePart append query, casPart, consistencyLevel)
  }

  /**
   * Generates a conditional query clause based on CQL lightweight transactions.
   * Compare and set transactions only get executed if a particular condition is true.
   *
   * @param clause The Compare-And-Set clause to append to the builder.
   * @return A conditional query, now bound by a compare-and-set part.
   */
  def onlyIf(clause: Table => CompareAndSetClause.Condition): ConditionalDeleteQuery[Table, Record, Limit, Order, Status, Chain] = {
    val query = QueryBuilder.Update.onlyIf(clause(table).qb)
    new ConditionalDeleteQuery(table, init, wherePart, casPart append query, consistencyLevel)
  }

  override val qb: CQLQuery = {
    (wherePart merge casPart) build init
  }
}

object DeleteQuery {

  type Default[T <: CassandraTable[T, _], R] = DeleteQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, NoPSQuery]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.Delete.delete(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

  def apply[T <: CassandraTable[T, _], R](table: T, col: String)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.Delete.deleteColumn(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString, col))
  }
}

sealed class ConditionalDeleteQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table,
  val init: CQLQuery,
  wherePart : WherePart = WherePart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None
 ) extends ExecutableStatement with Batchable {

  override val qb: CQLQuery = {
    (wherePart merge casPart) build init
  }

  final def and(clause: Table => CompareAndSetClause.Condition): ConditionalDeleteQuery[Table, Record, Limit, Order, Status, Chain] = {
    new ConditionalDeleteQuery(
      table,
      init,
      wherePart,
      casPart append QueryBuilder.Update.and(clause(table).qb),
      consistencyLevel
    )
  }
}

