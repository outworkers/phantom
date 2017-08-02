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
import cats.syntax.functor._
import com.datastax.driver.core.Session
import com.outworkers.phantom.batch.BatchQuery
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatements, GuavaAdapter, PromiseInterface, QueryCollection, ResultQueryInterface}
import com.outworkers.phantom.builder.query.prepared.{ExecutablePreparedQuery, ExecutablePreparedSelectQuery}
import com.outworkers.phantom.builder.query.{RootQuery, SelectQuery}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.{==:==, SingleGeneric, TableHelper}
import com.outworkers.phantom.{CassandraTable, ResultSet, Row}
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

  implicit class BatchOps[Status <: ConsistencyBound](val query: BatchQuery[Status]) {
    def future()(implicit session: Session, ctx: ExecutionContextExecutor): F[ResultSet] = {
      adapter.executeBatch(query.makeBatch())
    }
  }

  implicit class RootQueryOps[
    Table <: CassandraTable[Table, _],
    Record,
    Status <: ConsistencyBound
  ](val query: RootQuery[Table, Record, Status]) {
    def future()(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): F[ResultSet] = adapter.fromGuava(query.executableQuery)
  }

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
  ) extends SelectQueryOps(query) {
    override def executableQuery: ExecutableCqlQuery = query.executableQuery
  }


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

  implicit class ExecutablePrepareQueryOps(query: ExecutablePreparedQuery) {
    def future()(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): F[ResultSet] = adapter.fromGuava(query.options(query.statement))
  }

  implicit class ExecutablePreparedSelect[
    Table <: CassandraTable[Table, _],
    R,
    Limit <: LimitBound
  ](query: ExecutablePreparedSelectQuery[Table, R, Limit]) extends ResultQueryInterface[F, Table, R, Limit] {
    override def fromRow(r: Row): R = query.fromRow(r)

    /**
      * Default asynchronous query execution method. This will convert the underlying
      * call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Scala Future that will be completed once the operation is completed on the
      * database end.
      *
      * The execution context of the transformation is provided by phantom via
      * based on the execution engine used.
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param ec The implicit Scala execution context.
      * @return An asynchronous Scala future wrapping the Datastax result set.
      */
    override def future()(
      implicit session: Session,
      ec: ExecutionContextExecutor
    ): F[ResultSet] = {
      adapter.fromGuava(query.options(query.st))
    }

    /**
      * Returns the first row from the select ignoring everything else
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param ev      The implicit limit for the query.
      * @param ec      The implicit Scala execution context.
      * @return A Scala future guaranteed to contain a single result wrapped as an Option.
      */
    override def one()(
      implicit session: Session,
      ev: Limit =:= Unlimited,
      ec: ExecutionContextExecutor
    ): F[Option[R]] = future() map (_.value().map(query.fn))

    override def executableQuery: ExecutableCqlQuery = query.executableQuery
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
