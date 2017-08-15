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

import cats.Monad
import com.datastax.driver.core.Statement
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatements, QueryCollection, QueryInterface}
import com.outworkers.phantom.builder.query.options.{CompressionStrategy, GcGraceSecondsBuilder, TablePropertyClause, TimeToLiveBuilder}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.finagle.execution.{TwitterFutureImplicits, TwitterQueryContext}
import com.outworkers.phantom.ops.DbOps
import com.twitter.concurrent.Spool
import com.twitter.conversions.time._
import com.twitter.util.{Duration => TwitterDuration, _}
import org.joda.time.Seconds
import shapeless.HList

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContextExecutor

package object finagle extends TwitterQueryContext with DefaultImports {

  implicit val twitterFutureMonad: Monad[Future] = TwitterFutureImplicits.monadInstance

  implicit class RootSelectBlockSpool[
    T <: CassandraTable[T, _],
    R
  ](val block: RootSelectBlock[T, R]) extends AnyVal {

    /**
      * Produces a Twitter Spool of [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @return
      */
    def fetchSpool(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace
    ): Future[Spool[Seq[R]]] = {
      block.all().future(modifier) flatMap { rs =>
        ResultSpool.spool(rs).map(spool => spool.map(_.map(block.all.fromRow)))
      }
    }

    /**
      * Produces a Twitter Spool of [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @return
      */
    def fetchSpool()(
      implicit session: Session,
      keySpace: KeySpace
    ): Future[Spool[Seq[R]]] = {
      block.all().future() flatMap { rs =>
        ResultSpool.spool(rs).map(spool => spool.map(_.map(block.all.fromRow)))
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

  implicit class AssignmentsUpdateQueryAugmenter[
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

  def cql(str: CQLQuery, options: QueryOptions = QueryOptions.empty): QueryInterface[Future] = new QueryInterface[Future]() {
    override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(str, options)
  }

  def cql(str: String): QueryInterface[Future] = cql(CQLQuery(str))

  implicit class ExecuteQueries[M[X] <: TraversableOnce[X]](val qc: QueryCollection[M]) extends AnyVal {
    def executable()(
      implicit ctx: ExecutionContextExecutor
    ): ExecutableStatements[Future, M] = new ExecutableStatements[Future, M](qc)

    def future()(implicit session: Session,
      fbf: CanBuildFrom[M[Future[ResultSet]], Future[ResultSet], M[Future[ResultSet]]],
      ebf: CanBuildFrom[M[Future[ResultSet]], ResultSet, M[ResultSet]]
    ): Future[M[ResultSet]] = executable().future()
  }

  implicit def dbToOps[DB <: Database[DB]](db: Database[DB]): DbOps[Future, DB, TwitterDuration] = {
    new DbOps[Future, DB, TwitterDuration](db) {

      override def execute[M[X] <: TraversableOnce[X]](col: QueryCollection[M])(
        implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
      ): ExecutableStatements[Future, M] = new ExecutableStatements[Future, M](col)

      override def defaultTimeout: TwitterDuration = 10.seconds

      override def await[T](f: Future[T], timeout: TwitterDuration): T = Await.result(f, timeout)
    }
  }
}
