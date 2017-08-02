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
import com.datastax.driver.core.Session
import com.outworkers.phantom.{CassandraTable, ResultSet}
import com.outworkers.phantom.builder.query.SelectQuery
import com.outworkers.phantom.builder.{ConsistencyBound, LimitBound, OrderBound, WhereBound}
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatements, GuavaAdapter, PromiseInterface, QueryCollection}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.{==:==, SingleGeneric, TableHelper}
import shapeless.{Generic, HList}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContextExecutor

abstract class QueryContext[P[_], F[_], Timeout](
  defaultTimeout: Timeout
)(
  implicit fMonad: Monad[F],
  val promiseInterface: PromiseInterface[P, F],
  val adapter: GuavaAdapter[F]
) { outer =>

  def executeStatements[M[X] <: TraversableOnce[X]](
    col: QueryCollection[M]
  )(implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]): ExecutableStatements[F, M] = {
    new ExecutableStatements[F, M](col)
  }

  def await[T](f: F[T], timeout: Timeout): T

  implicit class SelectOps[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](
    override val query: SelectQuery[Table, Record, Limit, Order, Status, Chain, PS]
  ) extends SelectQueryOps(query)


  implicit class DatabaseOperation[DB <: Database[DB]](
    override val db: DB
  ) extends DbOps[F, DB, Timeout](db) {
    override def execute[M[X] <: TraversableOnce[X]](col: QueryCollection[M])(
      implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
    ): ExecutableStatements[F, M] = {
      executeStatements(col)
    }

    override def defaultTimeout: Timeout = outer.defaultTimeout

    override def await[T](f: F[T], timeout: Timeout): T = outer.await(f, timeout)
  }

  implicit class CassandraTableStoreMethods[T <: CassandraTable[T, R], R](val table: T) {

    def storeRecord[V1, Repr <: HList, HL <: HList, Out <: HList](input: V1)(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric.Aux[V1, Repr, HL, Out],
      ctx: ExecutionContextExecutor,
      ev: Out ==:== Repr
    ): F[ResultSet] = adapter.fromGuava(table.store(input).executableQuery)

    def storeRecords[M[X] <: TraversableOnce[X], V1, Repr <: HList, HL <: HList, Out <: HList](inputs: M[V1])(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric.Aux[V1, Repr, HL, Out],
      ev: Out ==:== Repr,
      ctx: ExecutionContextExecutor,
      cbfB: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]],
      cbf: CanBuildFrom[M[V1], ResultSet, M[ResultSet]],
      fbf: CanBuildFrom[M[F[ResultSet]], F[ResultSet], M[F[ResultSet]]],
      fbf2: CanBuildFrom[M[F[ResultSet]], ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = {

      val builder = cbfB()

      for (el <- inputs) {
        builder += table.store(el).executableQuery
      }

      executeStatements[M](new QueryCollection[M](builder.result()))(cbfB).future()(session, ctx, fbf, fbf2)
    }
  }
}
