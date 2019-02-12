/*
 * Copyright 2013 - 2019 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.clauses._
import com.outworkers.phantom.builder.ops.TokenizerKey
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.builder.query.prepared.{PrepareMark, PreparedBlock, PreparedFlattener}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.{CassandraTable, Row}
import org.joda.time.DateTime
import shapeless.ops.hlist.{Prepend, Reverse}
import shapeless.{::, =:!=, HList, HNil}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{FiniteDuration => ScalaDuration}
import scala.concurrent.duration._

case class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](table: Table,
  init: CQLQuery,
  tokens: List[TokenizerKey],
  usingPart: UsingPart = UsingPart.empty,
  wherePart: WherePart = WherePart.empty,
  private[phantom] val setPart: SetPart = SetPart.empty,
  casPart: CompareAndSetPart = CompareAndSetPart.empty,
  options: QueryOptions = QueryOptions.empty
) extends RootQuery[Table, Record, Status] with Batchable {

  val qb: CQLQuery = usingPart merge setPart merge wherePart build init

  /**
    * The where method of a select query.
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  def where[
    RR,
    HL <: HList,
    Out <: HList
  ](condition: Table => QueryCondition[HL])(implicit
    ev: Chain =:= Unchainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, Out] = {
    copy(
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      tokens = tokens ::: condition(table).tokens
    )
  }

  /**
    * The where method of a select query.
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  def and[
    RR,
    HL <: HList,
    Out <: HList
  ](condition: Table => QueryCondition[HL])(implicit
    ev: Chain =:= Chainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): UpdateQuery[Table, Record, Limit, Order, Status, Chainned, Out] = {
    copy(
      wherePart = wherePart append QueryBuilder.Update.and(condition(table).qb),
      tokens = tokens ::: condition(table).tokens
    )
  }

  final def modify[
    HL <: HList,
    Out <: HList
  ](clause: Table => UpdateClause.Condition[HL])(
    implicit prepend: Prepend.Aux[HL, HNil, Out]
  ): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, Out] = {
    AssignmentsQuery(
      table = table,
      init = init,
      tokens = tokens,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart appendConditionally(clause(table).qb, !clause(table).skipped),
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
  def onlyIf(
    clause: Table => CompareAndSetClause.Condition
  ): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, HNil] = {
    ConditionalQuery(
      table = table,
      init = init,
      tokens = tokens,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      options = options
    )
  }

  def ttl(seconds: Long): UpdateQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    copy(setPart = setPart append QueryBuilder.ttl(seconds.toString))
  }

  def withOptions(opts: QueryOptions => QueryOptions): UpdateQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    copy(options = opts(this.options))
  }

  def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified,
    session: Session
  ): UpdateQuery[Table, Record, Limit, Order, Specified, Chain, PS] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options, tokens)
}

sealed case class AssignmentsQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList,
  ModifyPrepared <: HList
](table: Table,
  init: CQLQuery,
  tokens: List[TokenizerKey],
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  private[phantom] val setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  options: QueryOptions
) extends RootQuery[Table, Record, Status] with Batchable {

  val qb: CQLQuery = usingPart merge setPart merge wherePart merge casPart build init

  final def and[
    HL <: HList,
    Out <: HList
  ](clause: Table => UpdateClause.Condition[HL])(
    implicit prepend: Prepend.Aux[HL, ModifyPrepared, Out]
  ): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, Out] = {
    copy(setPart = setPart appendConditionally (clause(table).qb, !clause(table).skipped))
  }

  /**
    * Allows setting a timestamp value in microseconds.
    * @param value The microsecond UTC timestamp of the time to use for the update query.
    * @return An assignments query with a timestamp value manually set.
    */
  final def timestamp(value: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    copy(usingPart = usingPart append QueryBuilder.timestamp(value.micros))
  }

  final def timestamp(value: DateTime): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    copy(usingPart = usingPart append QueryBuilder.timestamp((value.getMillis * 1000).micros))
  }

  final def ttl(mark: PrepareMark): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, Long :: PS, ModifyPrepared] = {
    copy(usingPart = usingPart append QueryBuilder.ttl(mark.qb.queryString))
  }

  final def ttl(seconds: Long): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    copy(usingPart = usingPart append QueryBuilder.ttl(seconds.toString))
  }

  final def ttl(duration: ScalaDuration): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ttl(duration.toSeconds)
  }

  /**
    * Prepares a statement synchronously.
    * @param session The session in which to execute the statement.
    * @param keySpace The key space in which to execute this statement.
    * @param rev Reverses the HList of prepared types provided in the WHERE .. AND chain.
    * @param rev2
    * @param prepend
    * @tparam RevSet
    * @tparam RevModified
    * @tparam Out
    * @return
    */
  def prepare[
    RevSet <: HList,
    RevModified <: HList,
    Out <: HList
  ]()(
    implicit session: Session,
    keySpace: KeySpace,
    //ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, RevSet],
    rev2: Reverse.Aux[ModifyPrepared, RevModified],
    prepend: Prepend.Aux[RevModified, RevSet, Out]
  ): PreparedBlock[Out] = {
    val flatten = new PreparedFlattener(qb)
    new PreparedBlock[Out](flatten.query, flatten.protocolVersion, options)
  }

  def prepareAsync[
    P[_],
    F[_],
    RevWhere <: HList,
    RevSet <: HList,
    Out <: HList
  ]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    //ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, RevWhere],
    rev2: Reverse.Aux[ModifyPrepared, RevSet],
    prepend: Prepend.Aux[RevSet, RevWhere, Out],
    fMonad: FutureMonad[F],
    adapter: GuavaAdapter[F],
    interface: PromiseInterface[P, F]
  ): F[PreparedBlock[Out]] = {
    val flatten = new PreparedFlattener(qb)

    flatten.async map { ps =>
      new PreparedBlock[Out](ps, flatten.protocolVersion, options)
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
  ): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ConditionalQuery(
      table = table,
      init = init,
      tokens = tokens,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart append QueryBuilder.Update.onlyIf(clause(table).qb),
      options = options
    )
  }

  def ifExists: ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ConditionalQuery(
      table = table,
      init = init,
      tokens = tokens,
      usingPart = usingPart,
      wherePart = wherePart,
      setPart = setPart,
      casPart = casPart append QueryBuilder.Update.ifExists,
      options = options
    )
  }

  def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified,
    session: Session
  ): AssignmentsQuery[Table, Record, Limit, Order, Specified, Chain, PS, ModifyPrepared] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options, tokens)
}

sealed case class ConditionalQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList,
  ModifyPrepared <: HList
](table: Table,
  init: CQLQuery,
  tokens: List[TokenizerKey],
  usingPart: UsingPart = UsingPart.empty,
  wherePart : WherePart = WherePart.empty,
  private[phantom] val setPart : SetPart = SetPart.empty,
  casPart : CompareAndSetPart = CompareAndSetPart.empty,
  options: QueryOptions
) extends RootQuery[Table, Record, Status] with Batchable {

  def qb: CQLQuery = usingPart merge setPart merge wherePart merge casPart build init

  final def and(
    clause: Table => CompareAndSetClause.Condition
  ): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    copy(casPart = casPart append QueryBuilder.Update.and(clause(table).qb))
  }

  def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified, session: Session
  ): ConditionalQuery[Table, Record, Limit, Order, Specified, Chain, PS, ModifyPrepared] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  def ttl(seconds: Long): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    copy(setPart = setPart append QueryBuilder.ttl(seconds.toString))
  }

  final def ttl(duration: ScalaDuration): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifyPrepared] = {
    ttl(duration.toSeconds)
  }

  def prepare[
    RevWhere <: HList,
    RevSet <: HList,
    QueryHL <: HList
  ]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, RevWhere],
    revModified: Reverse.Aux[ModifyPrepared, RevSet],
    prepender: Prepend.Aux[RevSet, RevWhere, QueryHL]
  ): PreparedBlock[QueryHL] = {
    val flatten = new PreparedFlattener(qb)
    new PreparedBlock(flatten.query, flatten.protocolVersion, options)
  }

  def prepareAsync[
    P[_],
    F[_],
    RevWhere <: HList,
    RevSet <: HList,
    QueryHL <: HList
  ]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    revWhere: Reverse.Aux[PS, RevWhere],
    revModified: Reverse.Aux[ModifyPrepared, RevSet],
    fMonad: FutureMonad[F],
    interface: PromiseInterface[P, F],
    prepender: Prepend.Aux[RevSet, RevWhere, QueryHL]
  ): F[PreparedBlock[QueryHL]] = {
    val flatten = new PreparedFlattener(qb)

    flatten.async map { ps =>
      new PreparedBlock(ps, flatten.protocolVersion, options)
    }
  }

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options, tokens)
}

object UpdateQuery {

  type Default[T <: CassandraTable[T, _], R] = UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = {
    new UpdateQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil](
      table = table,
      init = QueryBuilder.Update.update(
        QueryBuilder.keyspace(keySpace.name, table.tableName).queryString
      ),
      tokens = Nil
    )
  }

}
