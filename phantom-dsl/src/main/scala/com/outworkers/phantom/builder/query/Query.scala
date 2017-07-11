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
import com.outworkers.phantom.{CassandraTable, Row}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.clauses.QueryCondition
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.ExecutableStatement
import shapeless.HList
import shapeless.ops.hlist.Prepend

import scala.annotation.implicitNotFound

abstract class RootQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](table: Table, val qb: CQLQuery, override val options: QueryOptions) extends ExecutableStatement {

  protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    S <: ConsistencyBound
  ] <: RootQuery[T, R, S]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    S <: ConsistencyBound
  ](t: T, q: CQLQuery, options: QueryOptions): QueryType[T, R, S]


  @implicitNotFound("You have already specified a ConsistencyLevel for this query")
  def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): QueryType[Table, Record, Specified]
}


abstract class Query[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](
  table: Table,
  override val qb: CQLQuery,
  row: Row => Record,
  usingPart: UsingPart = UsingPart.empty,
  override val options: QueryOptions
) extends ExecutableStatement {

  protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ] <: Query[T, R, L, O, S, C, P]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ](t: T, q: CQLQuery, r: Row => R, usingPart: UsingPart, options: QueryOptions): QueryType[T, R, L, O, S, C, P]

  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  def consistencyLevel_=(level: ConsistencyLevel)
    (implicit ev: Status =:= Unspecified, session: Session): QueryType[Table, Record, Limit, Order, Specified, Chain, PS] = {
    if (session.protocolConsistency) {
      create[Table, Record, Limit, Order, Specified, Chain, PS](
        table,
        CQLQuery.empty,
        row,
        usingPart,
        options.consistencyLevel_=(level)
      )
    } else {
      create[Table, Record, Limit, Order, Specified, Chain, PS](
        table,
        CQLQuery.empty,
        row,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        options
      )
    }
  }

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
  ](
    condition: Table => QueryCondition[HL]
  )(implicit
    ev: Chain =:= Unchainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): QueryType[Table, Record, Limit, Order, Status, Chainned, Out]

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
  ](
    condition: Table => QueryCondition[HL]
  )(implicit
    ev: Chain =:= Chainned,
    prepend: Prepend.Aux[HL, PS, Out]
  ): QueryType[Table, Record, Limit, Order, Status, Chainned, Out]


  def ttl(seconds: Long): QueryType[Table, Record, Limit, Order, Status, Chain, PS] = {
    create[Table, Record, Limit, Order, Status, Chain, PS](
      table,
      qb,
      row,
      usingPart append QueryBuilder.ttl(seconds.toString),
      options
    )
  }

  def ttl(duration: scala.concurrent.duration.FiniteDuration): QueryType[Table, Record, Limit, Order, Status, Chain, PS] = {
    ttl(duration.toSeconds)
  }
}

trait Batchable { self: ExecutableStatement => }
