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

import com.datastax.driver.core.{PagingState, Session, SimpleStatement, Statement, Duration => DatastaxDuration, ResultSet => DatastaxResultSet}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.outworkers.phantom.batch.{BatchQuery, BatchWithQuery}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.execution.{ExecutableStatement, ExecutableStatements}
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


  protected[this] def batchToPromise(batch: BatchWithQuery)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = {
    Manager.logger.debug(s"Executing query: ${batch.debugString}")
    statementToPromise(batch.statement)
  }

  protected[this] def statementToPromise(str: Statement)(
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
    ): Future[Spool[Seq[R]]] = {
      block.all().future() flatMap {
        rs => ResultSpool.spool(rs).map(spool => spool.map(_.map(block.all.fromRow)))
      }
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
      statementToPromise(block.st)
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
