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

import com.datastax.driver.core.Statement
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution._
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

  implicit val twitterFutureMonad: FutureMonad[Future] = TwitterFutureImplicits.monadInstance

  /**
    * Method that allows executing a simple query straight from text, by-passing the entire mapping layer
    * but leveraging the execution layer.
    * @param str The input [[CQLQuery]] to execute.
    * @param options The [[QueryOptions]] to pass alongside the query.
    * @return A future wrapping a database result set.
    */
  def cql(
    str: CQLQuery,
    options: QueryOptions
  ): QueryInterface[Future] = new QueryInterface[Future]() {
    override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(str, options)
  }

  /**
    * Method that allows executing a simple query straight from text, by-passing the entire mapping layer
    * but leveraging the execution layer.
    * @param str The input [[CQLQuery]] to execute.
    * @param options The [[QueryOptions]] to pass alongside the query.
    * @return A future wrapping a database result set.
    */
  def cql(
    str: String,
    options: QueryOptions = QueryOptions.empty
  ): QueryInterface[Future] = cql(CQLQuery(str), options)

  implicit class SpoolSelectQueryOps[
    P[_],
    F[_],
    T <: CassandraTable[T, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](
    val query: SelectQuery[T, Record, Limit, Order, Status, Chain, PS]
  ) extends AnyVal {
    /**
      * Produces a Twitter Spool of [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
      * @return
      */
    def fetchSpool(modifier: Statement => Statement)(
      implicit session: Session
    ): Future[Spool[Seq[Record]]] = {
      query.future(modifier) flatMap { rs =>
        ResultSpool.spool(rs).map(spool => spool.map(_.map(query.fromRow)))
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
    ): Future[Spool[Seq[Record]]] = {
      query.future() flatMap { rs =>
        ResultSpool.spool(rs).map(spool => spool.map(_.map(query.fromRow)))
      }
    }

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
    T <: CassandraTable[T, Record],
    Record,
    Status <: ConsistencyBound,
    PS <: HList

  ](val query: InsertQuery[T, Record, Status, PS]) extends AnyVal {
    def ttl(duration: com.twitter.util.Duration): InsertQuery[T, Record, Status, PS] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class UpdateQueryAugmenter[
    T <: CassandraTable[T, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList
  ](val query: UpdateQuery[T, Record, Limit, Order, Status, Chain, PS]) extends AnyVal {

    def ttl(duration: com.twitter.util.Duration): UpdateQuery[T, Record, Limit, Order, Status, Chain, PS] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class AssignmentsUpdateQueryAugmenter[
    T <: CassandraTable[T, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    ModifiedPrepared <: HList
  ](val query: AssignmentsQuery[T, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared]) extends AnyVal {

    def ttl(duration: TwitterDuration): AssignmentsQuery[T, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
      query.ttl(duration.inSeconds)
    }
  }

  implicit class ConditionalUpdateQueryAugmenter[
    T <: CassandraTable[T, _],
    Record,
    Limit <: LimitBound,
    Order <: OrderBound,
    Status <: ConsistencyBound,
    Chain <: WhereBound,
    PS <: HList,
    ModifiedPrepared <: HList
  ](val query: ConditionalQuery[T, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared]) extends AnyVal {

    def ttl(duration: TwitterDuration): ConditionalQuery[T, Record, Limit, Order, Status, Chain, PS, ModifiedPrepared] = {
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
