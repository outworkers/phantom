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
import com.datastax.driver.core.Session
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.builder.query.prepared.{PreparedFlattener, PreparedSelectBlock}
import com.outworkers.phantom.builder.query.{LimitedPart, SelectQuery}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.{CassandraTable, Row}
import shapeless.ops.hlist.Reverse
import shapeless.{=:!=, HList, HNil}

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContextExecutor

class SelectQueryOps[
  P[_],
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
  fMonad: FutureMonad[F]
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
  ): F[Option[Record]] = {
    val enforceLimit = if (query.count) LimitedPart.empty else query.limitedPart append QueryBuilder.limit(1.toString)
    singleFetch(adapter.fromGuava(query.copy(limitedPart = enforceLimit).executableQuery.statement()))
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
  ): F[Option[Inner]] = {
    val enforceLimit = if (query.count) LimitedPart.empty else query.limitedPart append QueryBuilder.limit(1.toString)
    optionalFetch(adapter.fromGuava(query.copy(limitedPart = enforceLimit).executableQuery.statement()))
  }

  override def fromRow(r: Row): Record = query.fromRow(r)

  def prepareAsync[Rev <: HList]()(
    implicit session: Session,
    executor: ExecutionContextExecutor,
    keySpace: KeySpace,
    ev: PS =:!= HNil,
    rev: Reverse.Aux[PS, Rev],
    fMonad: FutureMonad[F],
    interface: PromiseInterface[P, F]
  ): F[PreparedSelectBlock[Table, Record, Limit, Rev]] = {
    val flatten = new PreparedFlattener(executableQuery.qb)

    flatten.async map { ps =>
      new PreparedSelectBlock[Table, Record, Limit, Rev](
        ps,
        flatten.protocolVersion,
        fromRow,
        executableQuery.options
      )
    }
  }

  override def executableQuery: ExecutableCqlQuery = query.executableQuery
}
