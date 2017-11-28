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

import com.datastax.driver.core.{Session, Statement}
import com.outworkers.phantom.builder.batch.BatchQuery
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.builder.query.prepared.{ExecutablePreparedQuery, ExecutablePreparedSelectQuery}
import com.outworkers.phantom.builder.query._
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
  implicit fMonad: FutureMonad[F],
  val promiseInterface: PromiseInterface[P, F]
) { outer =>

  implicit val adapter: GuavaAdapter[F] = promiseInterface.adapter

  def executeStatements[M[X] <: TraversableOnce[X]](
    col: QueryCollection[M]
  ): ExecutableStatements[F, M] = {
    new ExecutableStatements[F, M](col)
  }

  /**
    * An abstract implementation for blockingly waiting for future completion.
    * We need this for synchronously prepared statements and other instances
    * and a mechanism to abstract over the various future backends.
    * @param f The underlying future to wait for.
    * @param timeout The amount of time to wait for.
    * @tparam T the type of the underlying future.
    * @return The underlying value if the future is successfully completed, or an error thrown otherwise.
    */
  def blockAwait[T](f: F[T], timeout: Timeout): T

  implicit class BatchOps[Status <: ConsistencyBound](val query: BatchQuery[Status]) {
    def future()(implicit session: Session, ctx: ExecutionContextExecutor): F[ResultSet] = {
      promiseInterface.adapter.executeBatch(query.makeBatch())
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
    ): F[ResultSet] = promiseInterface.adapter.fromGuava(query.executableQuery)
  }

  implicit class RootSelectBlockOps[
    Table <: CassandraTable[Table, Record],
    Record
  ](val block: RootSelectBlock[Table, Record])(
    implicit keySpace: KeySpace
  ) extends ResultQueryInterface[F, Table, Record, Unlimited] {
    override def fromRow(r: Row): Record = block.rowFunc(r)

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
      ev: =:=[Unlimited, Unlimited],
      ec: ExecutionContextExecutor
    ): F[Option[Record]] = block.all().one()

    override def executableQuery: ExecutableCqlQuery = block.all().executableQuery
  }

  implicit def updateOps[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](query: UpdateQuery[T, R, L, O, S, Chain, PS]): UpdateIncompleteQueryOps[P, F] = {
    new UpdateIncompleteQueryOps(query.executableQuery, query.setPart)
  }

  implicit def assignmentUpdateOps[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    MP <: HList
  ](query: AssignmentsQuery[T, R, L, O, S, Chain, PS, MP]): UpdateIncompleteQueryOps[P, F] = {
    new UpdateIncompleteQueryOps(query.executableQuery, query.setPart)
  }

  implicit def conditionalUpdateOps[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    MP <: HList
  ](query: ConditionalQuery[T, R, L, O, S, Chain, PS, MP]): UpdateIncompleteQueryOps[P, F] = {
    new UpdateIncompleteQueryOps(query.executableQuery, query.setPart)
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
    override val db: Database[DB]
  ) extends DbOps[F, DB, Timeout](db) {
    override def execute[M[X] <: TraversableOnce[X]](col: QueryCollection[M])(
      implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
    ): ExecutableStatements[F, M] = {
      executeStatements(col)
    }

    override def defaultTimeout: Timeout = outer.defaultTimeout

    override def await[T](f: F[T], timeout: Timeout): T = outer.blockAwait(f, timeout)
  }

  implicit class ExecutablePrepareQueryOps(query: ExecutablePreparedQuery) extends QueryInterface[F] {
    override def executableQuery: ExecutableCqlQuery = query.executableQuery

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
      promiseInterface.adapter.fromGuava(query.options(query.st))
    }

    /**
      * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Scala Future that will be completed once the operation is completed on the
      * database end.
      *
      * The execution context of the transformation is provided by phantom via
      * based on the execution engine used.
      *
      * @param modifyStatement The function allowing to modify underlying [[Statement]]
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Scala executor.
      * @return An asynchronous Scala future wrapping the Datastax result set.
      */
    override def future(modifyStatement: Statement => Statement)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): F[ResultSet] = promiseInterface.adapter.fromGuava(modifyStatement(query.options(query.st)))
  }

  implicit class ExecutablePreparedSelect[
    Table <: CassandraTable[Table, _],
    R,
    Limit <: LimitBound
  ](query: ExecutablePreparedSelectQuery[Table, R, Limit]) extends ResultQueryInterface[F, Table, R, Limit] {
    override def fromRow(r: Row): R = query.fn(r)

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
      promiseInterface.adapter.fromGuava(query.options(query.st))
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

  implicit class CreateQueryOps[
    Table <: CassandraTable[Table, Record],
    Record,
    Consistency <: ConsistencyBound
  ](val query: CreateQuery[Table, Record, Consistency])(
    implicit keySpace: KeySpace
  ) extends MultiQueryInterface[Seq, F] {

    /**
      * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Scala Future that will be completed once the operation is completed on the
      * database end.
      *
      * The execution context of the transformation is provided by phantom via
      * based on the execution engine used.
      *
      * @param modifier The function allowing to modify underlying [[Statement]].
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Scala executor.
      * @return An asynchronous Scala future wrapping the Datastax result set.
      */
    override def future(modifier: Statement => Statement)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): F[Seq[ResultSet]] = {
      for {
        tableCreationQuery <- adapter.fromGuava(modifier(ExecutableCqlQuery(query.qb, query.options).statement()))
        secondaryIndexes <- new ExecutableStatements(query.indexList).future()
        sasiIndexes <- new ExecutableStatements(query.table.sasiQueries()).future()
      } yield Seq(tableCreationQuery) ++ secondaryIndexes ++ sasiIndexes
    }

    override def future()(
      implicit session: Session,
      ctx: ExecutionContextExecutor
    ): F[Seq[ResultSet]] = {
      for {
        tableCreationQuery <- adapter.fromGuava(query.executableQuery)
        secondaryIndexes <- new ExecutableStatements(query.indexList).future()
        sasiIndexes <- new ExecutableStatements(query.table.sasiQueries()).future()
      } yield Seq(tableCreationQuery) ++ secondaryIndexes ++ sasiIndexes
    }
  }

  implicit class CassandraTableStoreMethods[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) {

    def createSchema(timeout: Timeout = defaultTimeout)(
      implicit session: Session,
      keySpace: KeySpace,
      ec: ExecutionContextExecutor
    ): Seq[ResultSet] = {
      blockAwait(table.autocreate(keySpace).future(), timeout)
    }

    def storeRecord[V1, Repr <: HList, HL <: HList, Out <: HList](input: V1)(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric.Aux[V1, Repr, HL, Out],
      ctx: ExecutionContextExecutor,
      ev: Out ==:== Repr
    ): F[ResultSet] = promiseInterface.adapter.fromGuava(table.store(input).executableQuery)

    def storeRecords[M[X] <: TraversableOnce[X], V1, Repr <: HList, HL <: HList, Out <: HList](inputs: M[V1])(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric.Aux[V1, Repr, HL, Out],
      ev: Out ==:== Repr,
      ctx: ExecutionContextExecutor,
      cbfB: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]],
      fbf: CanBuildFrom[M[F[ResultSet]], F[ResultSet], M[F[ResultSet]]],
      fbf2: CanBuildFrom[M[F[ResultSet]], ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = {

      val queries = (cbfB() /: inputs)((acc, el) => acc += table.store(el).executableQuery)

      executeStatements[M](new QueryCollection[M](queries.result())).future()(session, ctx, fbf, fbf2)
    }
  }

  implicit class QueryCollectionOps[M[X] <: TraversableOnce[X]](val col: QueryCollection[M]) {

    def future()(
      implicit session: Session,
      ec: ExecutionContextExecutor,
      fbf: CanBuildFrom[M[F[ResultSet]], F[ResultSet], M[F[ResultSet]]],
      ebf: CanBuildFrom[M[F[ResultSet]], ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = executeStatements(col).future()

    def sequence()(
      implicit session: Session,
      ec: ExecutionContextExecutor,
      cbf: CanBuildFrom[M[ExecutableCqlQuery], ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = executeStatements(col).sequence()

  }
}
