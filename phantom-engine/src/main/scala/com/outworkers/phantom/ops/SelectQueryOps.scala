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
package com.outworkers.phantom.ops

import cats.Monad
import com.datastax.driver.core.{Session, Statement}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{GuavaAdapter, ResultQueryInterface}
import com.outworkers.phantom.builder.query.{LimitedPart, QueryOptions, SelectQuery}
import com.outworkers.phantom.{CassandraTable, ResultSet, Row}
import shapeless.HList

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContextExecutor, Future}

class SelectQueryOps[
  F[_],
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound,
  PS <: HList
](
  val query: SelectQuery[Table, Record, Limit, Order, Status, Chain, PS]
)(
  implicit adapter: GuavaAdapter[F],
  fMonad: Monad[F]
) extends ResultQueryInterface[F, Table, Record, Limit] {

  /**
    * Returns the first row from the select ignoring everything else
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ev The implicit limit for the query.
    * @param ec The implicit Scala execution context.
    * @return A Scala future guaranteed to contain a single result wrapped as an Option.
    */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def one()(
    implicit session: Session,
    ev: Limit =:= Unlimited,
    ec: ExecutionContextExecutor
  ): Future[Option[Record]] = {
    val enforceLimit = if (query.count) LimitedPart.empty else query.limitedPart append QueryBuilder.limit(1.toString)
    query.copy(limitedPart = enforceLimit).singleFetch()
  }

  /**
    * Returns the first row from the select ignoring everything else
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ev The implicit limit for the query.
    * @param ec The implicit Scala execution context.
    * @return A Scala future guaranteed to contain a single result wrapped as an Option.
    */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def aggregate[Inner]()(
    implicit session: Session,
    ev: Limit =:= Unlimited,
    opt: Record <:< Option[Inner],
    ec: ExecutionContextExecutor
  ): Future[Option[Inner]] = {
    val enforceLimit = if (query.count) LimitedPart.empty else query.limitedPart append QueryBuilder.limit(1.toString)
    query.copy(limitedPart = enforceLimit).optionalFetch()
  }

  override def fromRow(r: Row): Record = query.fromRow(r)

  override def fromGuava(in: Statement)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet] = adapter.fromGuava(in, query.options)

  override def options: QueryOptions = query.options

  override def qb: CQLQuery = query.qb
}
