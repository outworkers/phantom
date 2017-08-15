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

import java.util.Random

import cats.Monad
import com.datastax.driver.core.{Statement, VersionNumber}
import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.clauses.{UpdateClause, WhereClause}
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatements, QueryCollection, QueryInterface}
import com.outworkers.phantom.builder.query.options.{CompressionStrategy, GcGraceSecondsBuilder, TablePropertyClause, TimeToLiveBuilder}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.{AbstractColColumn, AbstractMapColumn, CounterColumn}
import com.outworkers.phantom.keys.Indexed
import com.outworkers.phantom.ops.DbOps
import com.twitter.concurrent.Spool
import com.twitter.conversions.time._
import com.twitter.util.{Duration => TwitterDuration, _}
import org.joda.time.{DateTimeZone, Seconds}
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
      * @param executor The implicit Java compatible Scala executor.
      * @return
      */
    def fetchSpool(modifier: Statement => Statement)(
      implicit session: Session,
      keySpace: KeySpace,
      executor: ExecutionContextExecutor
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
      * @param executor The implicit Java compatible Scala executor.
      * @return
      */
    def fetchSpool()(
      implicit session: Session,
      keySpace: KeySpace,
      executor: ExecutionContextExecutor
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

  implicit class CounterOperations[
    Owner <: CassandraTable[Owner, Record],
    Record
  ](val col: CounterColumn[Owner, Record]) extends AnyVal {
    final def +=[T : Numeric](value: T): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Update.increment(col.name, value.toString))
    }

    final def increment[T : Numeric](value: T): UpdateClause.Default = +=(value)

    final def -=[T : Numeric](value: T): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Update.decrement(col.name, value.toString))
    }

    final def decrement[T : Numeric](value: T): UpdateClause.Default = -=(value)
  }

  /**
    * Augments Cassandra VersionNumber descriptors to support simple comparison of versions.
    * This allows for operations that can differ based on the Cassandra version used by the session.
    *
    * @param version The Cassandra version number.
    */
  implicit class VersionAugmenter(val version: VersionNumber) extends AnyVal {
    def <(other: VersionNumber): Boolean = version.compareTo(other) == -1
    def ===(other: VersionNumber): Boolean = version.compareTo(other) == 0
    def > (other: VersionNumber): Boolean = version.compareTo(other) == 1

    def >= (other: VersionNumber): Boolean = {
      version.compareTo(other) >= 0
    }
  }

  implicit class DateTimeAugmenter(val date: DateTime) extends AnyVal {
    def timeuuid(): UUID = {
      val random = new Random()
      new UUID(UUIDs.startOf(date.getMillis).getMostSignificantBits, random.nextLong())
    }
  }

  implicit class UUIDAugmenter(val uid: UUID) extends AnyVal {
    def datetime: DateTime = new DateTime(UUIDs.unixTimestamp(uid), DateTimeZone.UTC)
  }

  implicit class ListLikeModifyColumn[
    Owner <: CassandraTable[Owner, Record],
    Record,
    RR
  ](val col: AbstractColColumn[Owner, Record, List, RR]) extends AnyVal {

    def prepend(value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.asCql(value :: Nil)))
    }

    def prepend(values: List[RR]): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.asCql(values)))
    }

    def append(value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.asCql(value :: Nil)))
    }

    def append(values: List[RR]): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.asCql(values)))
    }

    def discard(value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.discard(col.name, col.asCql(value :: Nil)))
    }

    def discard(values: List[RR]): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.discard(col.name, col.asCql(values)))
    }

    def setIdx(i: Int, value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.setIdX(col.name, i.toString, col.valueAsCql(value)))
    }
  }

  implicit class SetLikeModifyColumn[
    Owner <: CassandraTable[Owner, Record],
    Record,
    RR
  ](val col: AbstractColColumn[Owner, Record, Set, RR]) extends AnyVal {

    def add(value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.add(col.name, Set(col.valueAsCql(value))))
    }

    def addAll(values: Set[RR]): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.add(col.name, values.map(col.valueAsCql)))
    }

    def remove(value: RR): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.remove(col.name, Set(col.valueAsCql(value))))
    }

    def removeAll(values: Set[RR]): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.remove(col.name, values.map(col.valueAsCql)))
    }
  }

  implicit class MapLikeModifyColumn[
    Owner <: CassandraTable[Owner, Record],
    Record,
    A,
    B
  ](val col: AbstractMapColumn[Owner, Record, A, B]) extends AnyVal {

    def set(key: A, value: B): UpdateClause.Default = {
      new UpdateClause.Condition(
        QueryBuilder.Collections.mapSet(
          col.name,
          col.keyAsCql(key).toString,
          col.valueAsCql(value)
        )
      )
    }

    def put(value: (A, B)): UpdateClause.Default = {
      val (k, v) = value

      new UpdateClause.Condition(QueryBuilder.Collections.put(
        col.name,
        col.keyAsCql(k).toString -> col.valueAsCql(v)
      )
      )
    }

    def putAll[L](values: L)(implicit ev1: L => Traversable[(A, B)]): UpdateClause.Default = {
      new UpdateClause.Condition(
        QueryBuilder.Collections.put(col.name, values.map { case (key, value) =>
          col.keyAsCql(key) -> col.valueAsCql(value)
        }.toSeq : _*)
      )
    }
  }

  implicit class SetConditionals[
    T <: CassandraTable[T, R],
    R, RR
  ](val col: AbstractColColumn[T, R, Set, RR]) extends AnyVal {

    /**
      * Generates a Set CONTAINS clause that can be used inside a CQL Where condition.
      * @param elem The element to check for in the contains clause.
      * @return A Where clause.
      */
    final def contains(elem: RR): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.contains(col.name, col.valueAsCql(elem))
      )
    }
  }

  /**
    * Definition used to cast an index map column with keys indexed to a query-able definition.
    * This will allow users to use "CONTAINS KEY" clauses to search for matches based on map keys.
    *
    * @param col The map column to cast to a Map column secondary index query.
    * @tparam T The Cassandra table inner type.
    * @tparam R The record type of the table.
    * @tparam K The type of the key held in the map.
    * @tparam V The type of the value held in the map.
    * @return A MapConditionals class with CONTAINS KEY support.
    */
  implicit class MapKeyConditionals[
    T <: CassandraTable[T, R],
    R,
    K,
    V
  ](val col: AbstractMapColumn[T, R, K, V] with Indexed with Keys) extends AnyVal {

    /**
      * Generates a Map CONTAINS KEY clause that can be used inside a CQL Where condition.
      * This allows users to lookup records by a KEY inside a map column of a table.
      *
      * Key support is not yet enabled in phantom because index generation has to be done differently.
      * Otherwise, there is no support for simultaneous indexing on both KEYS and VALUES of a MAP column.
      * This limitation will be lifted in the future.
      *
      * @param elem The element to check for in the contains clause.
      * @return A Where clause.
      */
    final def containsKey(elem: K): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.containsKey(col.name, col.keyAsCql(elem))
      )
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

  implicit val context: ExecutionContextExecutor = Manager.scalaExecutor
}
