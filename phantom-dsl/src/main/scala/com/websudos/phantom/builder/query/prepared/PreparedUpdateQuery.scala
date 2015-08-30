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
package com.websudos.phantom.builder.query.prepared

import com.datastax.driver.core.{ConsistencyLevel, Row, Session}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.clauses.{CompareAndSetClause, UpdateClause, WhereClause}
import com.websudos.phantom.builder.query._
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound

class PreparedUpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table,
  init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None
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
  ] = PreparedUpdateQuery[T, R, L, O, S, C]


  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](t: T, q: CQLQuery, r: Row => R, consistencyLevel: Option[ConsistencyLevel] = None): QueryType[T, R, L, O, S, C] = {
    new PreparedUpdateQuery[T, R, L, O, S, C](
      table = t,
      init = q,
      usingPart = UsingPart.empty,
      wherePart = WherePart.empty,
      setPart = SetPart.empty,
      casPart = CompareAndSetPart.empty,
      consistencyLevel = consistencyLevel
    )
  }

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  override def where(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Unchainned): PreparedUpdateQuery[Table, Record, Limit, Order, Status, Chainned] = {
    new PreparedUpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.where(condition(table).qb),
      setPart,
      casPart,
      consistencyLevel
    )
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @return A SelectCountWhere.
   */
  @implicitNotFound("You have to use an where clause before using an AND clause")
  override def and(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Chainned): PreparedUpdateQuery[Table, Record, Limit, Order, Status, Chainned] = {
    new PreparedUpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.and(condition(table).qb),
      setPart,
      casPart,
      consistencyLevel
    )
  }

  final def modify(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    new AssignmentsQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart append QueryBuilder.Update.set(clause(table).qb),
      casPart,
      consistencyLevel
    )
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
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      consistencyLevel
    )
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
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None
   ) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    new AssignmentsQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart append clause(table).qb,
      casPart,
      consistencyLevel
    )
  }

  final def timestamp(value: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart append QueryBuilder.using(QueryBuilder.timestamp(init, value.toString)),
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart,
      consistencyLevel = consistencyLevel
    )
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
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      consistencyLevel
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): AssignmentsQuery[Table, Record, Limit, Order, Specified, Chain] = {
    if (session.v3orNewer) {
      new AssignmentsQuery(
        table,
        init,
        usingPart,
        wherePart,
        setPart,
        casPart,
        Some(level)
      )
    } else {
      new AssignmentsQuery(
        table,
        init,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        wherePart,
        setPart,
        casPart
      )
    }
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
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val consistencyLevel: Option[ConsistencyLevel] = None
   ) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(clause: Table => CompareAndSetClause.Condition): ConditionalQuery[Table, Record, Limit, Order, Status, Chain] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.and(clause(table).qb),
      consistencyLevel
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): ConditionalQuery[Table, Record, Limit, Order, Specified, Chain] = {
    if (session.v3orNewer) {
      new ConditionalQuery(
        table,
        init,
        usingPart,
        wherePart,
        setPart,
        casPart,
        consistencyLevel
      )
    } else {
      new ConditionalQuery(
        table,
        init,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        wherePart,
        setPart,
        casPart
      )
    }
  }

}

object PreparedUpdateQuery {

  type Default[T <: CassandraTable[T, _], R] = PreparedUpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): PreparedUpdateQuery.Default[T, R] = {
    new PreparedUpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned](
      table,
      QueryBuilder.Update.update(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

}
