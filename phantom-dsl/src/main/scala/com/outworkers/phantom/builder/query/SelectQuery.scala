/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
import com.outworkers.phantom.builder.clauses._
import com.outworkers.phantom.builder.primitives.Primitives.{LongPrimitive, StringPrimitive}
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.builder.query.prepared.{PrepareMark, PreparedFlattener, PreparedSelectBlock}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.builder.{ConsistencyBound, LimitBound, OrderBound, WhereBound, _}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.{CassandraTable, Row}
import shapeless.ops.hlist.{Prepend, Reverse}
import shapeless.{::, =:!=, HList, HNil}

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContextExecutor

case class SelectQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](
  protected[phantom] val table: Table,
  protected[phantom] val rowFunc: Row => Record,
  init: CQLQuery,
  protected[phantom] val wherePart: WherePart = WherePart.empty,
  protected[phantom] val orderPart: OrderPart = OrderPart.empty,
  protected[phantom] val limitedPart: LimitedPart = LimitedPart.empty,
  protected[phantom] val filteringPart: FilteringPart = FilteringPart.empty,
  protected[phantom] val usingPart: UsingPart = UsingPart.empty,
  protected[phantom] val count: Boolean = false,
  override val options: QueryOptions = QueryOptions.empty
) extends Query[Table, Record, Limit, Order, Status, Chain, PS](
  table, qb = init,
  rowFunc,
  usingPart,
  options
) {

  def fromRow(row: Row): Record = rowFunc(row)

  override val qb: CQLQuery = {
    (wherePart merge orderPart merge limitedPart merge filteringPart merge usingPart) build init
  }

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ] = SelectQuery[T, R, L, O, S, C, P]

  def allowFiltering(): SelectQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    copy(filteringPart = filteringPart append QueryBuilder.Select.allowFiltering())
  }

  def prepare[Rev <: HList]()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev]
  ): PreparedSelectBlock[Table, Record, Limit, Rev] = {
    val flatten = new PreparedFlattener(qb)
    new PreparedSelectBlock[Table, Record, Limit, Rev] (flatten.query, flatten.protocolVersion, rowFunc, options)
  }

  def prepareAsync[P[_], F[_], Rev <: HList]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev],
    fMonad: FutureMonad[F],
    interface: PromiseInterface[P, F]
  ): F[PreparedSelectBlock[Table, Record, Limit, Rev]] = {
    val flatten = new PreparedFlattener(qb)

    flatten.async map { ps =>
      new PreparedSelectBlock[Table, Record, Limit, Rev](ps, flatten.protocolVersion, rowFunc, options)
    }
  }

  /**
    * The where method of a select query.
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  override def where[
    RR,
    HL <: HList,
    Out <: HList
  ](condition: Table => QueryCondition[HL])(
    implicit ev: Chain =:= Unchainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): QueryType[Table, Record, Limit, Order, Status, Chainned, Out] = {
    copy(wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb))
  }

  /**
    * The where method of a select query.
    * @param condition A where clause condition restricted by path dependant types.
    * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
    * @return
    */
  override def and[
    RR,
    HL <: HList,
    Out <: HList
  ](condition: Table => QueryCondition[HL])(
    implicit ev: Chain =:= Chainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): QueryType[Table, Record, Limit, Order, Status, Chainned, Out] = {
    copy(wherePart = wherePart append QueryBuilder.Update.and(condition(table).qb))
  }

  def using(clause: UsingClause.Condition): SelectQuery[Table, Record, Limit, Order, Status, Chainned, PS] = {
    copy(usingPart = usingPart append clause.qb)
  }

  def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified,
    session: Session
  ): SelectQuery[Table, Record, Limit, Order, Specified, Chain, PS] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  @implicitNotFound("A limit was already specified for this query.")
  final def limit(ps: PrepareMark)(
    implicit ev: Limit =:= Unlimited
  ): QueryType[Table, Record, Limited, Order, Status, Chain, Int ::PS] = {
    copy(limitedPart = limitedPart append QueryBuilder.limit(ps.qb.queryString))
  }


  @implicitNotFound("A limit was already specified for this query.")
  def limit(limit: Int)(
    implicit ev: Limit =:= Unlimited
  ): QueryType[Table, Record, Limited, Order, Status, Chain, PS] = {
    copy(limitedPart = limitedPart append QueryBuilder.limit(limit.toString))
  }


  @implicitNotFound("You have already defined an ordering clause on this query.")
  final def orderBy(clauses: (Table => OrderingClause.Condition)*)(
    implicit ev: Order =:= Unordered
  ): SelectQuery[Table, Record, Limit, Ordered, Status, Chain, PS] = {
    copy(orderPart = orderPart append clauses.map(_(table).qb).toList)
  }

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options)
}

private[phantom] class RootSelectBlock[
  T <: CassandraTable[T, _],
  R
](table: T, val rowFunc: Row => R, columns: List[String], clause: Option[CQLQuery] = None) {

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  def all()(implicit keySpace: KeySpace): SelectQuery.Default[T, R] = {
    clause match {
      case Some(opt) =>
        new SelectQuery(
          table,
          rowFunc,
          QueryBuilder.Select.select(table.tableName, keySpace.name, opt)
        )
      case None =>
        new SelectQuery(
          table,
          rowFunc,
          QueryBuilder.Select.select(table.tableName, keySpace.name, columns: _*)
        )
    }
  }

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  def distinct()(implicit keySpace: KeySpace): SelectQuery.Default[T, R] = {
    new SelectQuery(table, rowFunc, QueryBuilder.Select.distinct(table.tableName, keySpace.name, columns: _*))
  }

  private[this] def extractCount(r: Row): Long = {
    LongPrimitive.fromRow(CQLSyntax.Selection.count, r).getOrElse(0L)
  }

  def json()(implicit keySpace: KeySpace): SelectQuery.Default[T, String] = {
    val jsonParser: (Row) => String = row => {
      StringPrimitive.deserialize(
        row.getBytesUnsafe(CQLSyntax.JSON_EXTRACTOR),
        row.version
      )
    }

    clause match {
      case Some(_) =>
        new SelectQuery(
          table,
          jsonParser,
          QueryBuilder.Select.selectJson(table.tableName, keySpace.name)
        )

      case None =>
        new SelectQuery(
          table,
          jsonParser,
          QueryBuilder.Select.selectJson(table.tableName, keySpace.name, columns: _*)
        )
    }
  }

  def function[RR](f1: TypedClause.Condition[RR])(
    implicit keySpace: KeySpace
  ): SelectQuery.Default[T, RR] = {
    new SelectQuery(
      table,
      f1.extractor,
      QueryBuilder.Select.select(table.tableName, keySpace.name, f1.qb),
      WherePart.empty,
      OrderPart.empty,
      LimitedPart.empty,
      FilteringPart.empty,
      UsingPart.empty,
      count = false
    )
  }

  def function[RR](f1: T => TypedClause.Condition[RR])(
    implicit keySpace: KeySpace
  ): SelectQuery.Default[T, RR] = {
    new SelectQuery(
      table,
      f1(table).extractor,
      QueryBuilder.Select.select(table.tableName, keySpace.name, f1(table).qb),
      WherePart.empty,
      OrderPart.empty,
      LimitedPart.empty,
      FilteringPart.empty,
      UsingPart.empty,
      count = false
    )
  }

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  def count()(implicit keySpace: KeySpace): SelectQuery.Default[T, Long] = {
    new SelectQuery(
      table,
      extractCount,
      QueryBuilder.Select.count(table.tableName, keySpace.name),
      WherePart.empty,
      OrderPart.empty,
      LimitedPart.empty,
      FilteringPart.empty,
      UsingPart.empty,
      count = true
    )
  }
}

object RootSelectBlock {

  def apply[T <: CassandraTable[T, _], R](table: T, columns: List[String], row: Row => R): RootSelectBlock[T, R] = {
    new RootSelectBlock(table, row, columns)
  }
}

object SelectQuery {

  type Default[T <: CassandraTable[T, _], R] = SelectQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, HNil]

  def apply[T <: CassandraTable[T, _], R](table: T, qb: CQLQuery, row: Row => R): SelectQuery.Default[T, R] = {
    new SelectQuery(table, row, qb)
  }
}


private[phantom] trait SelectImplicits {
  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  final implicit def rootSelectBlockToSelectQuery[
    T <: CassandraTable[T, _],
    R
  ](root: RootSelectBlock[T, R])(implicit keySpace: KeySpace): SelectQuery.Default[T, R] = {
    root.all
  }
}
