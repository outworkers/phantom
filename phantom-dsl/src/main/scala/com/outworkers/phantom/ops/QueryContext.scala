/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
import com.outworkers.phantom.builder.query.CreateQuery.DelegatedCreateQuery
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.builder.query.prepared.{ExecutablePreparedQuery, ExecutablePreparedSelectQuery}
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.{SingleGeneric, TableHelper}
import com.outworkers.phantom.{CassandraTable, ResultSet, Row}
import shapeless.{Generic, HList}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.collection.compat._
import scala.collection.Seq

abstract class QueryContext[P[_], F[_], Timeout](
  defaultTimeout: Timeout
)(
  implicit fMonad: FutureMonad[F],
  val promiseInterface: PromiseInterface[P, F]
) { outer =>

  type QueryNotExecuted = _root_.com.outworkers.phantom.ops.QueryNotExecuted
  type AsciiValue = com.outworkers.phantom.builder.primitives.AsciiValue
  val AsciiValue = com.outworkers.phantom.builder.primitives.AsciiValue
  type Payload = com.outworkers.phantom.builder.query.Payload
  val Payload = com.outworkers.phantom.builder.query.Payload

  type ListValue[T] = com.outworkers.phantom.builder.query.prepared.ListValue[T]
  val ListValue = com.outworkers.phantom.builder.query.prepared.ListValue

  implicit val adapter: GuavaAdapter[F] = promiseInterface.adapter

  def executeStatements[M[X] <: IterableOnce[X]](
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
    def future()(implicit session: Session, ctx: ExecutionContext): F[ResultSet] = {
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
      ctx: ExecutionContext
    ): F[ResultSet] = promiseInterface.adapter.fromGuava(query.executableQuery)
  }

  implicit class RootSelectBlockOps[
    Table <: CassandraTable[Table, _],
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
      ec: ExecutionContext
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
    MP <: HList,
    TTL <: HList
  ](query: AssignmentsQuery[T, R, L, O, S, Chain, PS, MP, TTL]): UpdateIncompleteQueryOps[P, F] = {
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
  ) extends DbOps[P, F, DB, Timeout](db) {
    override def execute[M[X] <: IterableOnce[X]](col: QueryCollection[M])(
      implicit cbf: BuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
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
      ec: ExecutionContext
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
      executor: ExecutionContext
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
      ec: ExecutionContext
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
      ec: ExecutionContext
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
      executor: ExecutionContext
    ): F[Seq[ResultSet]] = QueryContext.create(query.delegate, Some(modifier))

    override def future()(
      implicit session: Session,
      ctx: ExecutionContext
    ): F[Seq[ResultSet]] = QueryContext.create(query.delegate)
  }

  implicit class CassandraTableStoreMethods[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) {

    def createSchema(timeout: Timeout = defaultTimeout)(
      implicit session: Session,
      keySpace: KeySpace,
      ctx: ExecutionContext
    ): Seq[ResultSet] = {
      blockAwait(table.autocreate(keySpace).future(), timeout)
    }

    def storeRecord[V1, Repr <: HList, HL <: HList](input: V1)(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric[V1, Repr, HL],
      ctx: ExecutionContext
    ): F[ResultSet] = promiseInterface.adapter.fromGuava(table.store(input).executableQuery)

    def storeRecords[
      M[X] <: IterableOnce[X],
      V1,
      Repr <: HList,
      HL <: HList
    ](inputs: M[V1])(
      implicit keySpace: KeySpace,
      session: Session,
      thl: TableHelper.Aux[T, R, Repr],
      gen: Generic.Aux[V1, HL],
      sg: SingleGeneric[V1, Repr, HL],
      ctx: ExecutionContext,
      cbfEntry: Factory[ExecutableCqlQuery, M[ExecutableCqlQuery]],
      cbfB: BuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]],
      fbf: Factory[F[ResultSet], M[F[ResultSet]]],
      fbf2: Factory[ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = {
      val queries = inputs.iterator.foldLeft(cbfEntry.newBuilder) { (acc, el) => acc += table.store(el).executableQuery }

      executeStatements[M](new QueryCollection[M](queries.result())).future()(session, ctx, fbf, fbf2)
    }
  }

  implicit class QueryCollectionOps[M[X] <: IterableOnce[X]](val col: QueryCollection[M]) {

    def future()(
      implicit session: Session,
      ec: ExecutionContext,
      fbf: Factory[F[ResultSet], M[F[ResultSet]]],
      ebf: Factory[ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = executeStatements(col).future()

    def sequence()(
      implicit session: Session,
      ec: ExecutionContext,
      cbf: Factory[ResultSet, M[ResultSet]]
    ): F[M[ResultSet]] = executeStatements(col).sequence()
  }


  implicit def optionNumeric[T](implicit ev: Numeric[T]): Numeric[Option[T]] = new Numeric[Option[T]] {
    override def plus(x: Option[T], y: Option[T]): Option[T] = {
      for (x1 <- x; y1 <- y) yield ev.plus(x1, y1)
    }

    override def minus(x: Option[T], y: Option[T]): Option[T] = {
      for (x1 <- x; y1 <- y) yield ev.minus(x1, y1)
    }

    override def times(x: Option[T], y: Option[T]): Option[T] = {
      for (x1 <- x; y1 <- y) yield ev.times(x1, y1)
    }

    override def negate(x: Option[T]): Option[T] = {
      x.map(ev.negate)
    }

    override def fromInt(x: Int): Option[T] = {
      Option(ev.fromInt(x))
    }

    override def toInt(x: Option[T]): Int = {
      x.fold(0)(ev.toInt)
    }

    override def toLong(x: Option[T]): Long = {
      x.fold(0L)(ev.toLong)
    }

    override def toFloat(x: Option[T]): Float = {
      x.fold(0F)(ev.toFloat)
    }

    override def toDouble(x: Option[T]): Double = {
      x.fold(0D)(ev.toDouble)
    }

    override def compare(x: Option[T], y: Option[T]): Int = {
      { for (x1 <- x; y1 <- y) yield ev.compare(x1, y1) } getOrElse 0
    }

    def parseString(str: String): Option[Option[T]] = ???
  }
}


object QueryContext {
  def create[P[_], F[_]](
    query: DelegatedCreateQuery,
    modifier: Option[Statement => Statement] = None
  )(
    implicit interface: PromiseInterface[P, F],
    futureMonad: FutureMonad[F],
    session: Session,
    ctx: ExecutionContext
  ): F[Seq[ResultSet]] = {

    implicit val adapter: GuavaAdapter[F] = interface.adapter

    for {
      tableCreationQuery <- adapter.fromGuava(query.executable, modifier)
      secondaryIndexes <- new ExecutableStatements(query.indexList).future()
      sasiIndexes <- new ExecutableStatements(query.sasiIndexes).future()
    } yield Seq(tableCreationQuery) ++ secondaryIndexes ++ sasiIndexes
  }
}