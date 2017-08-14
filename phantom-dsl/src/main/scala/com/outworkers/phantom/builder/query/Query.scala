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

import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.clauses.QueryCondition
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.{CassandraTable, Row}
import shapeless.HList
import shapeless.ops.hlist.Prepend

abstract class RootQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
] extends SessionAugmenterImplicits {

  def queryString: String = executableQuery.qb.terminate.queryString

  def executableQuery: ExecutableCqlQuery
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
  val qb: CQLQuery,
  row: Row => Record,
  usingPart: UsingPart = UsingPart.empty,
  val options: QueryOptions
) extends RootQuery[Table, Record, Status] {

  protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound,
    P <: HList
  ] <: Query[T, R, L, O, S, C, P]

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
}

private[phantom] trait Batchable {
  def executableQuery: ExecutableCqlQuery
}
