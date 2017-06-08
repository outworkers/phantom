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
package com.outworkers.phantom

import java.util.{List => JavaList}

import com.datastax.driver.core.{PagingState, Session, SimpleStatement, Statement, Duration => DatastaxDuration, ResultSet => DatastaxResultSet}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.outworkers.phantom.batch.{BatchQuery, BatchWithQuery}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.options.{CompressionStrategy, GcGraceSecondsBuilder, TablePropertyClause, TimeToLiveBuilder}
import com.outworkers.phantom.builder.query.prepared.ExecutablePreparedSelectQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}
import com.outworkers.phantom.database.ExecutableCreateStatementsList
import com.twitter.concurrent.Spool
import com.twitter.util.{Duration => TwitterDuration, _}
import org.joda.time.Seconds
import shapeless.HList

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContextExecutor

package object finagle extends SessionAugmenterImplicits {

  protected[this] type Modifier = Statement => Statement

  protected[this] def batchToPromise(batch: BatchWithQuery)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = {
    Manager.logger.debug(s"Executing query: ${batch.debugString}")
    batchToPromise(batch.statement)
  }

  protected[this] def batchToPromise(str: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = {
    Manager.logger.debug(s"Executing query: $str")

    val promise = Promise[ResultSet]()
    val future = session.executeAsync(str)

    val callback = new FutureCallback[DatastaxResultSet] {
      def onSuccess(result: DatastaxResultSet): Unit = {
        promise update Return(ResultSet(result, session.protocolVersion))
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise update Throw(err)
      }
    }
    Futures.addCallback(future, callback, executor)
    promise
  }

  implicit class RootSelectBlockSpool[
    T <: CassandraTable[T, _],
    R
  ](val block: RootSelectBlock[T, R]) extends AnyVal {
    /**
      * Produces a Twitter Spool of [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java compatible Scala executor.
      * @return
      */
    def fetchSpool()(
      implicit session: Session,
      keySpace: KeySpace,
      executor: ExecutionContextExecutor
    ): Future[Spool[R]] = {
      block.all().execute() flatMap {
        rs => ResultSpool.spool(rs).map(_ map block.all.fromRow)
      }
    }
  }

  implicit class ExexcutableStatementAugmenter(val query: ExecutableStatement) extends AnyVal {
    /**
      * Default asynchronous query execution method based on Twitter Future API. This will convert the underlying
      * call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Twitter Future that will be completed once the operation is completed on the
      * database end.
      *
      * Unlike Scala Futures, Twitter Futures and operations on them do not require an implicit context.
      * Instead, the context propagates from one future to another inside a flatMap chain which means
      * all operations(map, flatMap) that originate on a Twitter Future obtained as the result of a database
      * call will execute inside [[Manager.scalaExecutor]].
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return
      */
    def execute()(implicit session: Session, executor: ExecutionContextExecutor): Future[ResultSet] = {
      batchToPromise(query.statement())
    }

    /**
      * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Scala Future that will be completed once the operation is completed on the
      * database end.
      *
      * The execution context of the transformation is provided by phantom via
      * [[Manager.scalaExecutor]] and it is recommended to
      * use [[com.outworkers.phantom.dsl.context]] for operations that chain
      * database calls.
      *
      * @param modifyStatement The function allowing to modify underlying [[Statement]]
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return
      */
    def execute(modifyStatement: Modifier)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[ResultSet] = {
      batchToPromise(modifyStatement(query.statement()))
    }
  }

  implicit class ExecutableQueryTwitterFuturesAugmenter[
    T <: CassandraTable[T, _],
    R,
    Limit <: LimitBound
  ](val query: ExecutableQuery[T, R, Limit]) extends AnyVal {

    protected[this] def singleResult(row: Option[Row]): Option[R] = row map query.fromRow

    protected[this] def directMapper(results: List[Row]): List[R] = {
      results map query.fromRow
    }

    private[phantom] def singleCollect()(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[Option[R]] = {
      query.execute() map { res => singleResult(res.value()) }
    }

    private[phantom] def singleOption[Inner]()(
      implicit session: Session,
      ev: R <:< Option[Inner],
      executor: ExecutionContextExecutor
    ): Future[Option[Inner]] = {
      query.execute() map (_.value() flatMap query.fromRow)
    }

    /**
      * Produces a [[com.twitter.concurrent.Spool]] of [R]ows
      * A spool is both lazily constructed and consumed, suitable for large
      * collections when using twitter futures.
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Spool of R.
      */
    def fetchSpool()(implicit session: Session, executor: ExecutionContextExecutor): Future[Spool[R]] = {
      query.execute() flatMap { rs =>
        ResultSpool.spool(rs).map(spool => spool map query.fromRow)
      }
    }


    /**
      * Returns a parsed sequence of [R]ows
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect()(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[List[R]] = {
      query.execute() map { rs => directMapper(rs.allRows) }
    }

    /**
      * Returns a parsed sequence of rows after the generated statement is modified by the modifier function.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect(modifyStatement: Modifier)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[List[R]] = {
      query.execute(modifyStatement) map (_.allRows().map(query.fromRow))
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect(pagingState: PagingState)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[List[R]] = {
      query.execute(_.setPagingState(pagingState)) map { rs => directMapper(rs.allRows) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord()(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[ListResult[R]] = {
      query.execute() map { rs => ListResult(directMapper(rs.allRows), rs) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(modifyStatement: Modifier)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[ListResult[R]] = {
      query.execute(modifyStatement) map { rs => ListResult(directMapper(rs.allRows), rs) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(pagingState: PagingState)(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[ListResult[R]] = {
      query.execute(_.setPagingState(pagingState)) map { rs =>
        ListResult(directMapper(rs.allRows()), rs)
      }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * A convenience method that exists solely to allow passing in an optional paging state.
      * This is not suitable for big results set
      *
      * @param state An optional paging state that will be added only if the state is defined.
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(state: Option[PagingState])(
      implicit session: Session,
      executor: ExecutionContextExecutor
    ): Future[ListResult[R]] = {
      state.fold(query.execute().map {
        set => ListResult(directMapper(set.allRows()), set)
      }) (state => query.execute(_.setPagingState(state)) map {
        set => ListResult(directMapper(set.allRows()), set)
      })
    }
}

  implicit class BatchQueryAugmenter[ST <: ConsistencyBound](val batch: BatchQuery[ST]) extends AnyVal {
    def execute()(implicit session: Session, executor: ExecutionContextExecutor): Future[ResultSet] = {
      batchToPromise(batch.makeBatch())
    }
  }

  implicit class ExecutableStatementListAugmenter(val list: ExecutableStatementList[Seq]) extends AnyVal {
    def execute()(implicit session: Session, executor: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
      Future.collect(list.queries.map(item => {
        batchToPromise(new SimpleStatement(item.terminate.queryString))
      }))
    }
  }

  implicit class CreateQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Status <: ConsistencyBound
  ](val query: CreateQuery[Table, Record, Status]) extends AnyVal {

    def execute()(
      implicit session: Session,
      ex: ExecutionContextExecutor
    ): Future[ResultSet] = {

      val root = batchToPromise(new SimpleStatement(query.qb.terminate.queryString))

      if (query.table.secondaryKeys.isEmpty) {
        root
      } else {
        root flatMap {
          res => query.indexList.execute() map { _ =>
            Manager.logger.debug(
              s"Creating secondary indexes on ${QueryBuilder.keyspace(query.keySpace.name, query.table.tableName).queryString}"
            )
            res
          }
        }
      }
    }
  }

  implicit class ExecutableCreateStatementsListAugmenter(
    val list: ExecutableCreateStatementsList
  ) extends AnyVal {
    def execute()(
      implicit session: Session,
      keySpace: KeySpace,
      executor: ExecutionContextExecutor
    ): Future[Seq[ResultSet]] = {
      Future.collect(list.queries(keySpace).map(_.execute()))
    }
  }

  implicit class InsertQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Status <: ConsistencyBound,
    PS <: HList
  ](val query: InsertQuery[Table, Record, Status, PS]) extends AnyVal {
    def ttl(duration: com.twitter.util.Duration): InsertQuery[Table, Record, Status, PS] = {
      query.ttl(duration.inSeconds)
    }
  }
  implicit class SelectQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val select: SelectQuery[Table, Record, Limit, Order, Status, Chain, PS]) extends AnyVal {
    /**
      * Returns the first row from the select ignoring everything else
      * This will always use a LIMIT 1 in the Cassandra query.
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param ev The implicit limit for the query.
      * @param executor The implicit Java executor.
      * @return
      */
    @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
    def get()(
      implicit session: Session,
      keySpace: KeySpace,
      ev: Limit =:= Unlimited,
      executor: ExecutionContextExecutor
    ): Future[Option[Record]] = {
      val enforceLimit = if (select.count) LimitedPart.empty else select.limitedPart append QueryBuilder.limit("1")

      new SelectQuery(
        table = select.table,
        rowFunc = select.rowFunc,
        init = select.init,
        wherePart = select.wherePart,
        orderPart = select.orderPart,
        limitedPart = enforceLimit,
        filteringPart = select.filteringPart,
        usingPart = select.usingPart,
        count = select.count,
        options = select.options
      ).singleCollect()
    }


    /**
      * Returns the first row from the select ignoring everything else
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param ev The implicit limit for the query.
      * @param ec The implicit Scala execution context.
      * @return A Scala future guaranteed to contain a single result wrapped as an Option.
      */
    @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
    def aggregated[Inner]()(
      implicit session: Session,
      ev: Limit =:= Unlimited,
      opt: Record <:< Option[Inner],
      ec: ExecutionContextExecutor
    ): Future[Option[Inner]] = select.singleOption()
  }

  implicit class GenericQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val query: Query[Table, Record, Limit, Order, Status, Chain, PS]) extends AnyVal {
    def ttl(duration: TwitterDuration): Query[Table, Record, Limit, Order, Status, Chain, PS] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class UpdateQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val query: UpdateQuery[Table, Record, Limit, Order, Status, Chain, PS]) extends AnyVal {

    def ttl(duration: com.twitter.util.Duration): UpdateQuery[Table, Record, Limit, Order, Status, Chain, PS] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class AssigmentsUpdateQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    ModifiedPrepared <: HList
  ](val query: AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared]) extends AnyVal {

    def ttl(duration: TwitterDuration): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class ConditionalUpdateQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    ModifiedPrepared <: HList
  ](val query: ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared]) extends AnyVal {

    def ttl(duration: TwitterDuration): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class PreparedSelectQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Limit <: LimitBound
  ](val block : ExecutablePreparedSelectQuery[Table, Record, Limit]) extends AnyVal {

    /**
      * Get the result of an operation as a Twitter Future.
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @param ev The implicit limit for the query.
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping the result.
      */
    def get()(
      implicit session: Session,
      ev: =:=[Limit, Unlimited],
      executor: ExecutionContextExecutor
    ): Future[Option[Record]] = block.singleCollect()

    def execute()(implicit session: Session, executor: ExecutionContextExecutor): Future[ResultSet] = {
      batchToPromise(block.st)
    }
  }

  implicit class TimeToLiveBuilderAugmenter(val builder: TimeToLiveBuilder) extends AnyVal {
    def eqs(duration: TwitterDuration): TablePropertyClause = builder.eqs(duration.inLongSeconds)
  }

  implicit class GcGraceSecondsBuilderAugmenter(val builder: GcGraceSecondsBuilder) extends AnyVal {
    def eqs(duration: TwitterDuration): TablePropertyClause = builder.eqs(Seconds.seconds(duration.inSeconds))
  }

  implicit class CompressionStrategyAugmenter[
    CS <: CompressionStrategy[CS]
  ](val strategy: CompressionStrategy[CS]) extends AnyVal {
    def chunk_length_kb(unit: StorageUnit): CompressionStrategy[CS] = {
      strategy.option(CQLSyntax.CompressionOptions.chunk_length_kb, unit.inKilobytes + "KB")
    }
  }
}
