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


import com.datastax.driver.core.{ConsistencyLevel, Row, Session}
import com.twitter.util.{Future => TwitterFuture}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.clauses.{OrderingClause, PreparedWhereClause, WhereClause}
import com.websudos.phantom.builder.query.prepared.{PNil, ParametricNode, ParametricValue}
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.util.Try


class SelectQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: PSBound
](
  table: Table,
  rowFunc: Row => Record,
  val init: CQLQuery,
  wherePart: WherePart = Defaults.EmptyWherePart,
  orderPart: OrderPart = Defaults.EmptyOrderPart,
  limitedPart: LimitedPart = Defaults.EmptyLimitPart,
  filteringPart: FilteringPart = Defaults.EmptyFilteringPart,
  count: Boolean = false,
  override val consistencyLevel: Option[ConsistencyLevel] = None,
  override val parameters: Seq[Any] = Seq.empty
) extends Query[Table, Record, Limit, Order, Status, Chain, PS](table, qb = init, rowFunc, consistencyLevel) with ExecutableQuery[Table,
  Record, Limit] {

  def fromRow(row: Row): Record = rowFunc(row)

  override val qb: CQLQuery = {
    (wherePart merge orderPart merge limitedPart merge filteringPart) build init
  }

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: PSBound
  ] = SelectQuery[T, R, L, O, S, C, P]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: PSBound
  ](t: T, q: CQLQuery, r: Row => R, level: Option[ConsistencyLevel]): QueryType[T, R, L, O, S, C, P] = {
    new SelectQuery[T, R, L, O, S, C, P](
      table = t,
      rowFunc = r,
      init = q,
      wherePart = wherePart,
      orderPart = orderPart,
      limitedPart = limitedPart,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = level
    )
  }

  def allowFiltering(): SelectQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart,
      orderPart = orderPart,
      limitedPart = limitedPart,
      filteringPart = filteringPart append QueryBuilder.Select.allowFiltering(),
      count = count,
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
  override def where(condition: Table => WhereClause.Condition)
                    (implicit ev: Chain =:= Unchainned): QueryType[Table, Record, Limit, Order, Status, Chainned, PS] = {
    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      orderPart = orderPart,
      limitedPart = limitedPart,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = consistencyLevel
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
                (implicit ev: Chain =:= Unchainned): SelectQuery[Table, Record, Limit, Order, Status, Chainned, PSUnspecified[ParametricValue[RR, PNil]]] = {
    new SelectQuery(
       table = table,
       rowFunc = rowFunc,
       init = init,
       wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
       orderPart = orderPart,
       limitedPart = limitedPart,
       filteringPart = filteringPart,
       count = count,
       consistencyLevel = consistencyLevel
     )
  }

  type **[PV, PN <: ParametricNode] = ParametricValue[PV, PN]

  @implicitNotFound("Parameters have been already specified.")
  def bind[V1](v1: V1)
                    (implicit ev: PS =:= PSUnspecified[V1 ** PNil]): QueryType[Table, Record, Limit, Order, Status, Chain, PSSpecified] = {
    new SelectQuery(
      table,
      rowFunc,
      init,
      wherePart,
      orderPart,
      limitedPart,
      filteringPart,
      count,
      consistencyLevel = consistencyLevel,
      parameters = Seq(v1)
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
                        (implicit ev: Chain =:= Unchainned): SelectQuery[Table, Record, Limit, Order, Status, Chainned, PSUnspecified[ParametricValue[RR, PNil]]] = {
    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      orderPart = orderPart,
      limitedPart = limitedPart,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = consistencyLevel
    )
  }

  /**
   * The and operator that adds parametric condition to the where predicates.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot add condition in this place of the query")
  def p_and[RR](condition: Table => WhereClause.ParametricCondition[RR])
                        (implicit ev: Chain =:= Unchainned): SelectQuery[Table, Record, Limit, Order, Status, Chainned, PSUnspecified[ParametricValue[RR, PNil]]] = {
    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
      orderPart = orderPart,
      limitedPart = limitedPart,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel
    )
  }

  @implicitNotFound("A limit was already specified for this query.")
  override def limit(limit: Int)
                    (implicit ev: Limit =:= Unlimited): QueryType[Table, Record, Limited, Order, Status, Chain, PS] = {
    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart,
      orderPart = orderPart,
      limitedPart = limitedPart append QueryBuilder.limit(limit),
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = consistencyLevel
    )
  }


  @implicitNotFound("You have already defined an ordering clause on this query.")
  final def orderBy(clause: Table => OrderingClause.Condition)
                   (implicit ev: Order =:= Unordered): SelectQuery[Table, Record, Limit, Ordered, Status, Chain, PS] = {
    new SelectQuery(
      table,
      rowFunc,
      init,
      wherePart,
      orderPart append QueryBuilder.Select.Ordering.orderBy(clause(table).qb),
      limitedPart,
      filteringPart,
      count,
      consistencyLevel = consistencyLevel
    )
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def one()(implicit session: Session, ctx: ExecutionContext, keySpace: KeySpace, ev: Limit =:= Unlimited): ScalaFuture[Option[Record]] = {
    val enforceLimit = if (count) Defaults.EmptyLimitPart else limitedPart append QueryBuilder.limit(1)

    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart,
      orderPart = orderPart,
      limitedPart = enforceLimit,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = consistencyLevel
    ).singleFetch()

  }

  /**
   * Returns the first row from the select ignoring everything else
   * This will always use a LIMIT 1 in the Cassandra query.
   * @param session The Cassandra session in use.
   * @return
   */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def get()(implicit session: Session, keySpace: KeySpace, ev: Limit =:= Unlimited): TwitterFuture[Option[Record]] = {
    val enforceLimit = if (count) Defaults.EmptyLimitPart else limitedPart append QueryBuilder.limit(1)

    new SelectQuery(
      table = table,
      rowFunc = rowFunc,
      init = init,
      wherePart = wherePart,
      orderPart = orderPart,
      limitedPart = enforceLimit,
      filteringPart = filteringPart,
      count = count,
      consistencyLevel = consistencyLevel
    ).singleCollect()
  }
}

private[phantom] class RootSelectBlock[T <: CassandraTable[T, _], R](table: T, rowFunc: Row => R, columns: List[String]) {

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  private[phantom] def all()(implicit keySpace: KeySpace): SelectQuery.Default[T, R] = {
    new SelectQuery(table, rowFunc, QueryBuilder.Select.select(table.tableName, keySpace.name, columns: _*))
  }

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  def distinct()(implicit keySpace: KeySpace): SelectQuery.Default[T, R] = {
    new SelectQuery(table, rowFunc, QueryBuilder.Select.distinct(table.tableName, keySpace.name, columns: _*))
  }

  private[this] def extractCount(r: Row): Long = {
    Try(r.getLong("count")).getOrElse(0L)
  }

  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  def count()(implicit keySpace: KeySpace): SelectQuery.Default[T, Long] = {
    new SelectQuery(
      table,
      extractCount,
      QueryBuilder.Select.count(table.tableName, keySpace.name),
      Defaults.EmptyWherePart,
      Defaults.EmptyOrderPart,
      Defaults.EmptyLimitPart,
      Defaults.EmptyFilteringPart,
      true
    )
  }
}

object RootSelectBlock {

  def apply[T <: CassandraTable[T, _], R](table: T, columns: List[String], row: Row => R): RootSelectBlock[T, R] = {
    new RootSelectBlock(table, row, columns)
  }
}

object SelectQuery {

  type Default[T <: CassandraTable[T, _], R] = SelectQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned, NoPSQuery]

  def apply[T <: CassandraTable[T, _], R](table: T, qb: CQLQuery, row: Row => R): SelectQuery.Default[T, R] = {
    new SelectQuery(table, row, qb)
  }
}


private[phantom] trait SelectImplicits {
  @implicitNotFound("You haven't provided a KeySpace in scope. Use a Connector to automatically inject one.")
  final implicit def rootSelectBlockToSelectQuery[T <: CassandraTable[T, _], R]( root: RootSelectBlock[T, R] )( implicit keySpace: KeySpace ): SelectQuery.Default[T, R] = {
    root.all
  }
}

class PreparedSelectQuery[
Table <: CassandraTable[Table, _],
Record,
Limit <: LimitBound,
Order <: OrderBound,
Status <: ConsistencyBound,
Chain <: WhereBound,
Parameters <: ParametricNode
](
   table: Table,
   rowFunc: Row => Record,
   init: CQLQuery,
   wherePart: WherePart = Defaults.EmptyWherePart,
   orderPart: OrderPart = Defaults.EmptyOrderPart,
   limitedPart: LimitedPart = Defaults.EmptyLimitPart,
   filteringPart: FilteringPart = Defaults.EmptyFilteringPart,
   count: Boolean = false,
   consistencyLevel: ConsistencyLevel = null
   ) {

  /**
   * The where method of a select query.
   * @param condition A where clause condition restricted by path dependant types.
   * @param ev An evidence request guaranteeing the user cannot chain multiple where clauses on the same query.
   * @return
   */
  @implicitNotFound("You cannot use multiple where clauses in the same builder")
  def where[RR](condition: Table => WhereClause.ParametricCondition[RR])(implicit ev: Chain =:= Unchainned): PreparedSelectQuery[Table, Record, Limit, Order, Status, Chainned, ParametricValue[RR, Parameters]] = {
    new PreparedSelectQuery(
                     table = table,
                     rowFunc = rowFunc,
                     init = init,
                     wherePart = wherePart append QueryBuilder.Update.where(condition(table).qb),
                     orderPart = orderPart,
                     limitedPart = limitedPart,
                     filteringPart = filteringPart,
                     count = count,
                     consistencyLevel
                   )
  }

}
