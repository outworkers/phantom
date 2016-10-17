/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom

import java.util.concurrent.Executor
import java.util.{List => JavaList}

import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.twitter.concurrent.Spool
import com.twitter.util._
import com.websudos.phantom.{CassandraTable, Manager}
import com.websudos.phantom.batch.BatchQuery
import com.websudos.phantom.builder._
import com.websudos.phantom.builder.query._
import com.websudos.phantom.builder.query.options.{CompressionStrategy, GcGraceSecondsBuilder, TablePropertyClause, TimeToLiveBuilder}
import com.websudos.phantom.builder.query.prepared.ExecutablePreparedSelectQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.database.ExecutableCreateStatementsList
import org.joda.time.Seconds
import shapeless.HList

import scala.annotation.implicitNotFound

package object finagle {

  protected[this] type Modifier = Statement => Statement

  protected[this] def twitterQueryStringExecuteToFuture(str: Statement)(
    implicit session: Session,
    keyspace: KeySpace,
    executor: Executor
  ): Future[ResultSet] = {
    Manager.logger.debug(s"Executing query: $str")

    val promise = Promise[ResultSet]()
    val future = session.executeAsync(str)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise update Return(result)
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
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return
      */
    def fetchSpool()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[Spool[R]] = {
      block.all().execute() flatMap {
        resultSet => ResultSpool.spool(resultSet).map(spool => spool map block.all.fromRow)
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
      * call will execute inside [[com.websudos.phantom.Manager.executor]].
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return
      */
    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[ResultSet] = {
      twitterQueryStringExecuteToFuture(query.statement())
    }

    /**
      * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
      * Scala Future that will be completed once the operation is completed on the
      * database end.
      *
      * The execution context of the transformation is provided by phantom via
      * [[com.websudos.phantom.Manager.scalaExecutor]] and it is recommended to
      * use [[com.websudos.phantom.dsl.context]] for operations that chain
      * database calls.
      *
      * @param modifyStatement The function allowing to modify underlying [[Statement]]
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return
      */
    def execute(modifyStatement: Modifier)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[ResultSet] = {
      twitterQueryStringExecuteToFuture(modifyStatement(query.statement()))
    }
  }

  implicit class ExecutableQueryTwitterFuturesAugmenter[
    T <: CassandraTable[T, _],
    R,
    Limit <: LimitBound
  ](val query: ExecutableQuery[T, R, Limit]) extends AnyVal {

    protected[this] def singleResult(row: Row): Option[R] = {
      if (Option(row).isDefined) Some(query.fromRow(row)) else None
    }

    protected[this] def directMapper(results: JavaList[Row]): List[R] = {
      List.tabulate(results.size())(index => query.fromRow(results.get(index)))
    }

    private[phantom] def singleCollect()(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[Option[R]] = {
      query.execute() map { res => singleResult(res.one) }
    }

    /**
      * Produces a [[com.twitter.concurrent.Spool]] of [R]ows
      * A spool is both lazily constructed and consumed, suitable for large
      * collections when using twitter futures.
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Spool of R.
      */
    def fetchSpool()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[Spool[R]] = {
      query.execute() flatMap {
        resultSet => ResultSpool.spool(resultSet).map(spool => spool map query.fromRow)
      }
    }


    /**
      * Returns a parsed sequence of [R]ows
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[List[R]] = {
      query.execute() map { resultSet => directMapper(resultSet.all) }
    }

    /**
      * Returns a parsed sequence of rows after the generated statement is modified by the modifier function.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect(modifyStatement: Modifier)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[List[R]] = {
      query.execute(modifyStatement) map { resultSet => directMapper(resultSet.all) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collect(pagingState: PagingState)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[List[R]] = {
      query.execute(st => st.setPagingState(pagingState)) map { resultSet => directMapper(resultSet.all) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[ListResult[R]] = {
      query.execute() map { resultSet => ListResult(directMapper(resultSet.all), resultSet) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(modifyStatement: Modifier)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[ListResult[R]] = {
      query.execute(modifyStatement) map { resultSet => ListResult(directMapper(resultSet.all), resultSet) }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * This is not suitable for big results set
      *
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(pagingState: PagingState)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[ListResult[R]] = {
      query.execute(st => st.setPagingState(pagingState)) map { resultSet =>
        ListResult(directMapper(resultSet.all), resultSet)
      }
    }

    /**
      * Returns a parsed sequence of [R]ows together with a result set.
      * A convenience method that exists solely to allow passing in an optional paging state.
      * This is not suitable for big results set
      *
      * @param state An optional paging state that will be added only if the state is defined.
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping a list of mapped results.
      */
    def collectRecord(state: Option[PagingState])(
      implicit session: Session,
      keySpace: KeySpace,
      executor: Executor
    ): Future[ListResult[R]] = {
      state.fold(query.execute().map {
        set => ListResult(directMapper(set.all), set)
      }) (state => query.execute(_.setPagingState(state)) map {
        set => ListResult(directMapper(set.all), set)
      })
    }
}

  implicit class BatchQueryAugmenter[ST <: ConsistencyBound](val batch: BatchQuery[ST]) extends AnyVal {
    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[ResultSet] = {
      twitterQueryStringExecuteToFuture(batch.makeBatch())
    }
  }

  implicit class ExecutableStatementListAugmenter(val list: ExecutableStatementList) extends AnyVal {
    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[Seq[ResultSet]] = {
      Future.collect(list.queries.map(item => {
        twitterQueryStringExecuteToFuture(new SimpleStatement(item.terminate().queryString))
      }))
    }
  }



  implicit class CreateQueryAugmenter[
    Table <: CassandraTable[Table, _],
    Record,
    Status <: ConsistencyBound
  ](val query: CreateQuery[Table, Record, Status]) extends AnyVal {

    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[ResultSet] = {
      if (query.table.secondaryKeys.isEmpty) {
        twitterQueryStringExecuteToFuture(new SimpleStatement(query.qb.terminate().queryString))
      } else {
        query.execute() flatMap {
          res => {
            query.indexList(keySpace.name).execute() map {
              _ => {
                Manager.logger.debug(s"Creating secondary indexes on ${QueryBuilder.keyspace(keySpace.name, query.table.tableName).queryString}")
                res
              }
            }
          }
        }
      }
    }
  }

  implicit class ExecutableCreateStatementsListAugmenter(val list: ExecutableCreateStatementsList) extends AnyVal {
    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[Seq[ResultSet]] = {
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
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param ev The implicit limit for the query.
      * @param executor The implicit Java executor.
      * @return
      */
    @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
    def get()(
      implicit session: Session,
      keySpace: KeySpace,
      ev: Limit =:= Unlimited,
      executor: Executor
    ): Future[Option[Record]] = {
      val enforceLimit = if (select.count) LimitedPart.empty else select.limitedPart append QueryBuilder.limit(1)

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
    def ttl(duration: com.twitter.util.Duration): Query[Table, Record, Limit, Order, Status, Chain, PS] = {
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

    def ttl(duration: Duration): AssignmentsQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
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

    def ttl(duration: Duration): ConditionalQuery[Table, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
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
      * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
      * @param ev The implicit limit for the query.
      * @param executor The implicit Java executor.
      * @return A Twitter future wrapping the result.
      */
    def get()(
      implicit session: Session,
      keySpace: KeySpace,
      ev: =:=[Limit, Unlimited],
      executor: Executor
    ): Future[Option[Record]] = {
      block.singleCollect()
    }

    def execute()(implicit session: Session, keySpace: KeySpace, executor: Executor): Future[ResultSet] = {
      twitterQueryStringExecuteToFuture(block.st)
    }
  }

  implicit class TimeToLiveBuilderAugmenter(val builder: TimeToLiveBuilder) extends AnyVal {
    def eqs(duration: com.twitter.util.Duration): TablePropertyClause = builder.eqs(duration.inLongSeconds)
  }

  implicit class GcGraceSecondsBuilderAugmenter(val builder: GcGraceSecondsBuilder) extends AnyVal {
    def eqs(duration: com.twitter.util.Duration): TablePropertyClause = builder.eqs(Seconds.seconds(duration.inSeconds))
  }

  implicit class CompressionStrategyAugmenter[
    CS <: CompressionStrategy[CS]
  ](val strategy: CompressionStrategy[CS]) extends AnyVal {
    def chunk_length_kb(unit: StorageUnit): CompressionStrategy[CS] = {
      strategy.option(CQLSyntax.CompressionOptions.chunk_length_kb, unit.inKilobytes + "KB")
    }
  }
}