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

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, Random}

import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.{VersionNumber, ConsistencyLevel => CLevel}
import com.outworkers.phantom
import com.outworkers.phantom.batch.Batcher
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.{UpdateClause, UsingClauseOperations, WhereClause}
import com.outworkers.phantom.builder.ops._
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.serializers.KeySpaceConstruction
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.DefaultVersions
import com.outworkers.phantom.macros.NamingStrategy
import org.joda.time.DateTimeZone
import shapeless.{::, HNil}

import scala.concurrent.ExecutionContextExecutor

object dsl extends ImplicitMechanism with CreateImplicits
  with SelectImplicits
  with Operators
  with UsingClauseOperations
  with KeySpaceConstruction
  with DeleteImplicits {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = phantom.CassandraTable[Owner, Record]
  implicit val strategy: NamingStrategy = NamingStrategy.CamelCase.caseInsensitive

  type Column[Owner <: CassandraTable[Owner, Record], Record, T] = com.outworkers.phantom.column.Column[Owner, Record, T]
  type PrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, T]
  type OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.outworkers.phantom.column.OptionalColumn[Owner, Record, T]

  type OptionalPrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, T]
  type BigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, BigDecimal]

  type BlobColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, ByteBuffer]
  type BigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, BigInt]
  type BooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Boolean]
  type DateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Date]
  type DateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, DateTime]
  type LocalDateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, LocalDate]
  type DoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Double]
  type FloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Float]
  type IntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Int]
  type SmallIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Short]
  type TinyIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Byte]
  type InetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, InetAddress]
  type LongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, Long]
  type StringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, String]
  type UUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, UUID]
  type CounterColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.CounterColumn[Owner, Record]
  type TimeUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.TimeUUIDColumn[Owner, Record]

  type OptionalBlobColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, ByteBuffer]
  type OptionalBigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigDecimal]
  type OptionalBigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigInt]
  type OptionalBooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Boolean]
  type OptionalDateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Date]
  type OptionalDateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, DateTime]
  type OptionalLocalDateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, LocalDate]
  type OptionalDoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Double]
  type OptionalFloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Float]
  type OptionalIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Int]
  type OptionalSmallIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Short]
  type OptionalTinyIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Byte]
  type OptionalInetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, InetAddress]
  type OptionalLongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, Long]
  type OptionalStringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, String]
  type OptionalUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, UUID]
  type OptionalTimeUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.outworkers.phantom.column.OptionalTimeUUIDColumn[Owner, Record]

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
  type CassandraConnection = com.outworkers.phantom.connectors.CassandraConnection
  val KeySpace = com.outworkers.phantom.connectors.KeySpace
  type RootConnector = com.outworkers.phantom.connectors.RootConnector

  val Version = DefaultVersions

  type ListResult[R] = com.outworkers.phantom.builder.query.ListResult[R]
  type IteratorResult[R] = com.outworkers.phantom.builder.query.IteratorResult[R]
  type RecordResult[R] = com.outworkers.phantom.builder.query.RecordResult[R]

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
    def percentile: CQLQuery = CQLQuery(percent.toString).append(CQLSyntax.CreateOptions.percentile)
  }

  implicit def primitiveToTokenOp[RR : Primitive](value: RR): TokenConstructor[RR :: HNil, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(Primitive[RR].asCql(value)))
  }

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
}
