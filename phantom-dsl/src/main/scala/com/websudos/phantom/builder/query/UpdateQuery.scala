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
import com.websudos.phantom.builder.clauses.{CompareAndSetClause, PreparedWhereClause, UpdateClause, WhereClause}
import com.websudos.phantom.builder.query.prepared.{PrepareMark, PreparedBlock}
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl.DateTime
import shapeless.ops.hlist.{Prepend, Reverse}
import shapeless.{::, =:!=, HList, HNil}

import scala.annotation.implicitNotFound
import scala.concurrent.duration.{FiniteDuration => ScalaDuration}

class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](table: Table,
  init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  wherePart: WherePart = WherePart.empty,
  private[phantom] val setPart: SetPart = SetPart.empty,
  casPart: CompareAndSetPart = CompareAndSetPart.empty,
  override val options: QueryOptions = QueryOptions.empty
) extends Query[Table, Record, Limit, Order, Status, Chain, PS](table, init, None.orNull, usingPart, options) with Batchable {

  override val qb: CQLQuery = {
    usingPart merge setPart merge wherePart build init
  }

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ] = UpdateQuery[T, R, L, O, S, C, P]

  def prepare()(implicit session: Session, keySpace: KeySpace, ev: PS =:!= HNil): PreparedBlock[PS] = {
    new PreparedBlock[PS](qb, options)
  }

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ](t: T, q: CQLQuery, r: Row => R, usingPart: UsingPart, options: QueryOptions): QueryType[T, R, L, O, S, C, P] = {
    new UpdateQuery[T, R, L, O, S, C, P](
      t,
      q,
      usingPart,
      wherePart,
      setPart,
      casPart,
      options
    )
  }

  override def ttl(seconds: Long): UpdateQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    new UpdateQuery(
      table,
      init, usingPart,
      wherePart,
      setPart append QueryBuilder.ttl(seconds.toString),
      casPart,
      options
    )
  }

  /**
   * The where method of a select query.
    *
    * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  override def where(condition: Table => WhereClause.Condition)
    (implicit ev: Chain =:= Unchainned): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    new UpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.where(condition(table).qb),
      setPart,
      casPart,
      options
    )
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
    *
    * @param condition The Query condition to execute, based on index operators.
   * @return A SelectCountWhere.
   */
  @implicitNotFound("You have to use an where clause before using an AND clause")
  override def and(condition: Table => WhereClause.Condition)
    (implicit ev: Chain =:= Chainned): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    new UpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.and(condition(table).qb),
      setPart,
      casPart,
      options
    )
  }

  /**
    * The where method of a select query that takes parametric predicate as an argument.
    *
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  def p_where[RR](condition: Table => PreparedWhereClause.ParametricCondition[RR])
    (implicit ev: Chain =:= Unchainned): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, RR :: PS] = {
    new UpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.where(condition(table).qb),
      setPart,
      casPart,
      options
    )
  }


  /**
    * The where method of a select query that takes parametric predicate as an argument.
    *
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  def p_and[RR](condition: Table => PreparedWhereClause.ParametricCondition[RR])
    (implicit ev: Chain =:= Chainned): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, RR :: PS] = {
    new UpdateQuery(
      table,
      init,
      usingPart,
      wherePart append QueryBuilder.Update.and(condition(table).qb),
      setPart,
      casPart,
      options
    )
  }


  final def modify(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, HNil] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart appendConditionally (QueryBuilder.Update.set(clause(table).qb), !clause(table).skipped),
      casPart = casPart,
      options = options
    )
  }

  final def p_modify[RR](
    clause: Table => PreparedWhereClause.ParametricCondition[RR]
  ): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, RR :: HNil] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart append QueryBuilder.Update.set(clause(table).qb),
      casPart = casPart,
      options = options
    )
  }

  /**
   * Generates a conditional query clause based on CQL lightweight transactions.
   * Compare and set transactions only get executed if a particular condition is true.
   *
    * @param clause The Compare-And-Set clause to append to the builder.
   * @return A conditional query, now bound by a compare-and-set part.
   */
  def onlyIf(clause: Table => CompareAndSetClause.Condition): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, HNil] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      options
    )
  }
}

sealed class AssignmentsQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList,
  ModifyPrepared <: HList
](table: Table,
  val init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  private[phantom] val setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val options: QueryOptions
) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(clause: Table => UpdateClause.Condition): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new AssignmentsQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart appendConditionally (clause(table).qb, !clause(table).skipped),
      casPart,
      options
    )
  }

  final def p_and[RR](
    clause: Table => PreparedWhereClause.ParametricCondition[RR]
  ): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, RR :: ModifyPrepared] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart append clause(table).qb,
      casPart = casPart,
      options = options
    )
  }

  final def timestamp(value: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart append QueryBuilder.timestamp(value),
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart,
      options = options
    )
  }

  final def timestamp(value: DateTime): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart append QueryBuilder.timestamp(value.getMillis),
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart,
      options = options
    )
  }

  def ttl(mark: PrepareMark): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, Long :: PS, ModifyPrepared] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart append QueryBuilder.ttl(mark.qb.queryString),
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart,
      options = options
    )
  }


  def ttl(seconds: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new AssignmentsQuery(
      table = table,
      init = init,
      usingPart = usingPart append QueryBuilder.ttl(seconds.toString),
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart,
      options = options
    )
  }

  def ttl(duration: ScalaDuration): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ttl(duration.toSeconds)
  }

  def prepare[
    Rev <: HList,
    Reversed <: HList
  ]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev],
    rev2: Reverse.Aux[ModifyPrepared, Reversed],
    prepend: Prepend[Reversed, Rev]
  ): PreparedBlock[prepend.Out] = {
    new PreparedBlock(qb, options)
  }

  /**
   * Generates a conditional query clause based on CQL lightweight transactions.
   * Compare and set transactions only get executed if a particular condition is true.
   *
    * @param clause The Compare-And-Set clause to append to the builder.
   * @return A conditional query, now bound by a compare-and-set part.
   */
  def onlyIf(clause: Table => CompareAndSetClause.Condition): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      options
    )
  }

  def ifExists: ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.ifExists,
      options
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)
    (implicit ev: Status =:= Unspecified, session: Session): AssignmentsQuery[Table, Record, Limit, Order, Specified, Chain, PS, ModifyPrepared] = {
    if (session.v3orNewer) {
      new AssignmentsQuery(
        table,
        init,
        usingPart,
        wherePart,
        setPart,
        casPart,
        options.consistencyLevel_=(level)
      )
    } else {
      new AssignmentsQuery(
        table,
        init,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        wherePart,
        setPart,
        casPart,
        options
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
  Chain <: WhereBound,
  PS <: HList,
  ModifyPrepared <: HList
](table: Table,
  val init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  private[phantom] val setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  override val options: QueryOptions
) extends ExecutableStatement with Batchable {

  val qb: CQLQuery = {
    usingPart merge setPart merge wherePart merge casPart build init
  }

  final def and(
    clause: Table => CompareAndSetClause.Condition
  ): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart,
      casPart append QueryBuilder.Update.and(clause(table).qb),
      options
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified, session: Session
  ): ConditionalQuery[Table, Record, Limit, Order, Specified, Chain, PS, ModifyPrepared] = {
    if (session.v3orNewer) {
      new ConditionalQuery(
        table = table,
        init = init,
        usingPart = usingPart,
        wherePart = wherePart,
        setPart = setPart,
        casPart = casPart,
        options.consistencyLevel_=(level)
      )
    } else {
      new ConditionalQuery(
        table = table,
        init = init,
        usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString),
        wherePart = wherePart,
        setPart = setPart,
        casPart = casPart,
        options = options
      )
    }
  }

  def ttl(seconds: Long): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    new ConditionalQuery(
      table,
      init,
      usingPart,
      wherePart,
      setPart append QueryBuilder.ttl(seconds.toString),
      casPart,
      options
    )
  }

  final def ttl(duration: ScalaDuration): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ttl(duration.toSeconds)
  }

  def prepare[Rev <: HList, Rev2 <: HList]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev],
    rev2: Reverse.Aux[ModifyPrepared, Rev2],
    prepend: Prepend[Rev2, Rev]
  ): PreparedBlock[prepend.Out] = {
    new PreparedBlock(qb, options)
  }

}

object UpdateQuery {

  type Default[T <: CassandraTable[T, _], R] = UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = {
    new UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil](
      table,
      QueryBuilder.Update.update(
        QueryBuilder.keyspace(keySpace.name, table.tableName).queryString
      )
    )
  }

}
