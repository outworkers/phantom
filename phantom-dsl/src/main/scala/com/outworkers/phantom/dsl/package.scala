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

import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.{Row, VersionNumber, ConsistencyLevel => CLevel}
import com.outworkers.phantom
import com.outworkers.phantom.batch.Batcher
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.{UpdateClause, UsingClauseOperations, WhereClause}
import com.outworkers.phantom.builder.ops._
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.serializers.{KeySpaceConstruction, RootSerializer}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column._
import com.outworkers.phantom.connectors.DefaultVersions
import org.joda.time.DateTimeZone
import shapeless.{::, HNil}

import scala.concurrent.ExecutionContextExecutor

package object dsl extends ImplicitMechanism with CreateImplicits
  with SelectImplicits
  with Operators
  with UsingClauseOperations
  with KeySpaceConstruction
  with DeleteImplicits {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = phantom.CassandraTable[Owner, Record]
  type ClusteringOrder = com.outworkers.phantom.keys.ClusteringOrder
  type Ascending = com.outworkers.phantom.keys.Ascending
  type Descending = com.outworkers.phantom.keys.Descending
  type PartitionKey = com.outworkers.phantom.keys.PartitionKey
  type PrimaryKey = com.outworkers.phantom.keys.PrimaryKey
  type Index = com.outworkers.phantom.keys.Index
  type Keys = com.outworkers.phantom.keys.Keys
  type Entries = com.outworkers.phantom.keys.Entries
  type StaticColumn = com.outworkers.phantom.keys.StaticColumn

  type Database[DB <: Database[DB]] = com.outworkers.phantom.database.Database[DB]
  type DatabaseProvider[DB <: Database[DB]] = com.outworkers.phantom.database.DatabaseProvider[DB]

  type DateTime = org.joda.time.DateTime
  type LocalDate = org.joda.time.LocalDate
  type DateTimeZone = org.joda.time.DateTimeZone
  type UUID = java.util.UUID
  type Row = com.datastax.driver.core.Row
  type ResultSet = com.datastax.driver.core.ResultSet
  type Session = com.datastax.driver.core.Session
  type KeySpace = com.outworkers.phantom.connectors.KeySpace
  val KeySpace = com.outworkers.phantom.connectors.KeySpace
  type RootConnector = com.outworkers.phantom.connectors.RootConnector

  val Version = DefaultVersions

  type ListResult[R] = com.outworkers.phantom.builder.query.ListResult[R]
  type IteratorResult[R] = com.outworkers.phantom.builder.query.IteratorResult[R]
  type RecordResult[R] = com.outworkers.phantom.builder.query.RecordResult[R]

  type Primitive[RR] = com.outworkers.phantom.builder.primitives.Primitive[RR]
  val Primitive = com.outworkers.phantom.builder.primitives.Primitive

  object ? extends PrepareMark
  case object Batch extends Batcher

  object ConsistencyLevel {
    val ALL = CLevel.ALL
    val Any = CLevel.ANY
    val ONE = CLevel.ONE
    val TWO = CLevel.TWO
    val THREE = CLevel.THREE
    val QUORUM = CLevel.QUORUM
    val LOCAL_QUORUM = CLevel.LOCAL_QUORUM
    val EACH_QUORUM = CLevel.EACH_QUORUM
    val LOCAL_SERIAL = CLevel.LOCAL_SERIAL
    val LOCAL_ONE = CLevel.LOCAL_ONE
    val SERIAL = CLevel.SERIAL
  }

  def cql(str: CQLQuery): ExecutableStatement = new ExecutableStatement {
    override def options: QueryOptions = QueryOptions.empty

    override def qb: CQLQuery = str
  }

  def cql(str: String): ExecutableStatement = cql(CQLQuery(str))

  type KeySpaceDef = com.outworkers.phantom.connectors.CassandraConnection
  val ContactPoint = com.outworkers.phantom.connectors.ContactPoint
  val ContactPoints = com.outworkers.phantom.connectors.ContactPoints

  implicit class RichNumber(val percent: Int) extends AnyVal {
    def percentile: CQLQuery = CQLQuery(percent.toString).pad.append(CQLSyntax.CreateOptions.percentile)
  }

  implicit def primitiveToTokenOp[RR : Primitive](value: RR): TokenConstructor[RR :: HNil, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(Primitive[RR].asCql(value)))
  }

  implicit class KeySpaceAugmenter(val k: KeySpace) extends AnyVal {
    def build: RootSerializer = new RootSerializer(k)
  }

  implicit def keyspaceInstanceToQueryBuilder(k: KeySpace): RootSerializer = new RootSerializer(k)

  implicit lazy val context: ExecutionContextExecutor = Manager.scalaExecutor

  implicit class PartitionTokenHelper[T](val col: AbstractColumn[T] with PartitionKey) extends AnyVal {

    def ltToken(value: T): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.lt(
          QueryBuilder.Where.token(col.name).queryString,
          QueryBuilder.Where.fcall(CQLSyntax.token, col.asCql(value)).queryString
        )
      )
    }

    def lteToken(value: T): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.lte(
          QueryBuilder.Where.token(col.name).queryString,
          QueryBuilder.Where.fcall(CQLSyntax.token, col.asCql(value)).queryString
        )
      )
    }

    def gtToken(value: T): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.gt(
          QueryBuilder.Where.token(col.name).queryString,
          QueryBuilder.Where.fcall(CQLSyntax.token, col.asCql(value)).queryString
        )
      )
    }

    def gteToken(value: T): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.gte(
          QueryBuilder.Where.token(col.name).queryString,
          QueryBuilder.Where.fcall(CQLSyntax.token, col.asCql(value)).queryString
        )
      )
    }

    def eqsToken(value: T): WhereClause.Condition = {
      new WhereClause.Condition(
        QueryBuilder.Where.eqs(
          QueryBuilder.Where.token(col.name).queryString,
          QueryBuilder.Where.fcall(CQLSyntax.token, col.asCql(value)).queryString
        )
      )
    }
  }

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
      new UpdateClause.Condition(QueryBuilder.Collections.mapSet(col.name, col.keyAsCql(key).toString, col.valueAsCql(value)))
    }

    def put(value: (A, B)): UpdateClause.Default = {
      new UpdateClause.Condition(QueryBuilder.Collections.put(
        col.name,
        col.keyAsCql(value._1).toString -> col.valueAsCql(value._2))
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

  implicit class SelectColumnRequired[
    Owner <: CassandraTable[Owner, Record],
    Record, T
  ](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  implicit class SelectColumnOptional[
    Owner <: CassandraTable[Owner, Record],
    Record, T
  ](col: OptionalColumn[Owner, Record, T]) extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}
