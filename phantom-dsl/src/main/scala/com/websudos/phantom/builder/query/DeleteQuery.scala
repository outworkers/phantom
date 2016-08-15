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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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

import com.datastax.driver.core.{ConsistencyLevel, Row, Session}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.clauses.{CompareAndSetClause, DeleteClause, PreparedWhereClause, WhereClause}
import com.websudos.phantom.builder.ops.MapKeyUpdateClause
import com.websudos.phantom.builder.query.prepared.PreparedBlock
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl.DateTime
import shapeless.ops.hlist.Reverse
import shapeless.{::, =:!=, HList, HNil}

import scala.annotation.implicitNotFound

class DeleteQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](table: Table,
  init: CQLQuery,
  wherePart : WherePart = WherePart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  usingPart: UsingPart = UsingPart.empty,
  override val options: QueryOptions = QueryOptions.empty
) extends Query[Table, Record, Limit, Order, Status, Chain, PS](table, init, None.orNull, usingPart, options) with Batchable {

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ] = DeleteQuery[T, R, L, O, S, C, P]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ](t: T, q: CQLQuery, r: Row => R, part: UsingPart, options: QueryOptions): QueryType[T, R, L, O, S, C, P] = {
    new DeleteQuery[T, R, L, O, S, C, P](t, q, wherePart, casPart, part, options)
  }

  def prepare[Rev <: HList]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): PreparedBlock[Rev] = {
    new PreparedBlock(qb, options)
  }

  def timestamp(time: Long): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    new DeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart,
      casPart = casPart,
      usingPart = usingPart append QueryBuilder.timestamp(time),
      options = options
    )
  }

  def timestamp(time: DateTime): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    timestamp(time.getMillis)
  }

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  override def where(condition: Table => WhereClause.Condition)(implicit ev: Chain =:= Unchainned): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    new DeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      casPart = casPart,
      usingPart = usingPart,
      options = options
    )
  }

  /**
   * The where method of a select query that takes parametric predicate as an argument.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  def p_where[RR](condition: Table => PreparedWhereClause.ParametricCondition[RR])
      (implicit ev: Chain =:= Unchainned): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, RR :: PS] = {
    new DeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      casPart = casPart,
      usingPart = usingPart,
      options = options
    )
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
    new DeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart append query,
      casPart = casPart,
      usingPart = usingPart,
      options = options
    )
  }

  /**
   * The and operator that adds parametric condition to the where predicates.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot add condition in this place of the query")
  def p_and[RR](condition: Table => PreparedWhereClause.ParametricCondition[RR])
      (implicit ev: Chain =:= Chainned): DeleteQuery[Table, Record, Limit, Order, Status, Chainned, RR :: PS] = {
    new DeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.and(condition(table).qb),
      casPart = casPart,
      options = options
    )
  }

  override def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified, session: Session
  ): DeleteQuery[Table, Record, Limit, Order, Specified, Chain, PS] = {
    if (session.v3orNewer) {
      new DeleteQuery(
        table = table,
        init = init,
        usingPart = usingPart,
        wherePart = wherePart,
        casPart = casPart,
        options = options.consistencyLevel_=(level)
      )
    } else {
      new DeleteQuery(
        table = table,
        init = init,
        usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString),
        wherePart = wherePart,
        casPart = casPart,
        options = options
      )
    }
  }


  /**
   * Generates a conditional query clause based on CQL lightweight transactions.
   * Compare and set transactions only get executed if a particular condition is true.
   *
   * @param clause The Compare-And-Set clause to append to the builder.
   * @return A conditional query, now bound by a compare-and-set part.
   */
  def onlyIf(
    clause: Table => CompareAndSetClause.Condition
    ): ConditionalDeleteQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    new ConditionalDeleteQuery(
      table = table,
      init = init,
      wherePart = wherePart,
      casPart = casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      usingPart = usingPart,
      options = options
    )
  }

  override val qb: CQLQuery = {
    (usingPart merge wherePart merge casPart) build init
  }

}


trait DeleteImplicits {
  implicit def columnUpdateClauseToDeleteCondition(clause: MapKeyUpdateClause[_, _]): DeleteClause.Condition = {
    new DeleteClause.Condition(QueryBuilder.Collections.mapColumnType(clause.column, clause.keyName))
  }

  implicit def columnClauseToDeleteCondition(col: AbstractColumn[_]): DeleteClause.Condition = {
    new DeleteClause.Condition(CQLQuery(col.name))
  }
}

object DeleteQuery {

  type Default[T <: CassandraTable[T, _], R] = DeleteQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.Delete.delete(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

  def apply[T <: CassandraTable[T, _], R](table: T, conds: CQLQuery*)(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    new DeleteQuery(table, QueryBuilder.Delete.delete(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString, conds))
  }
}

sealed class ConditionalDeleteQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](table: Table,
  val init: CQLQuery,
  wherePart : WherePart = WherePart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  usingPart: UsingPart = UsingPart.empty,
  override val options: QueryOptions
 ) extends ExecutableStatement with Batchable {

  override val qb: CQLQuery = {
    (usingPart merge wherePart merge casPart) build init
  }

  final def and(clause: Table => CompareAndSetClause.Condition): ConditionalDeleteQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    new ConditionalDeleteQuery(
      table,
      init,
      wherePart,
      casPart append QueryBuilder.Update.and(clause(table).qb),
      usingPart,
      options
    )
  }
}

