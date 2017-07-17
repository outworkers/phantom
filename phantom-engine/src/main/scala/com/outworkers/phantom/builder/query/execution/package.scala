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

import cats.Monad
import cats.implicits._
import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder._
import shapeless.HList

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContextExecutor, Future}

package object execution {

  implicit class FunctorOps[F[_], T](val in: F[T])(implicit fMonad: Monad[F]) {
    def zipWith[O, R](other: F[O])(fn: (T, O) => R): F[R] = {
      for (r1 <- in; r2 <- other) yield fn(r1, r2)
    }
  }


  implicit class SelectQueryOps[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val query: SelectQuery[Table, Record, Limit, Order, Status, Chain, PS]) extends AnyVal {
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

      new SelectQuery(
        table = query.table,
        rowFunc = query.rowFunc,
        init = init,
        wherePart = wherePart,
        orderPart = orderPart,
        limitedPart = enforceLimit,
        filteringPart = filteringPart,
        usingPart = usingPart,
        count = count,
        options = options
      ).optionalFetch()
    }
  }
}
