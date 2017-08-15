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

import com.datastax.driver.core.{ConsistencyLevel => CLevel}
import com.outworkers.phantom
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.batch.Batcher
import com.outworkers.phantom.builder.clauses.{UsingClauseOperations, WhereClause}
import com.outworkers.phantom.builder.ops._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.query.sasi.{DefaultSASIOps, Mode}
import com.outworkers.phantom.builder.query.{CreateImplicits, DeleteImplicits, SelectImplicits}
import com.outworkers.phantom.builder.serializers.{KeySpaceConstruction, RootSerializer}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.{AbstractColumn, Column, OptionalColumn}
import com.outworkers.phantom.connectors.DefaultVersions
import shapeless.{::, HNil}

trait DefaultImports extends ImplicitMechanism
  with CreateImplicits
  with SelectImplicits
  with Operators
  with UsingClauseOperations
  with KeySpaceConstruction
  with DeleteImplicits
  with DefaultSASIOps {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = phantom.CassandraTable[Owner, Record]
  type Table[Owner <: Table[Owner, Record], Record] = phantom.Table[Owner, Record]

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
  type Row = com.outworkers.phantom.Row
  type ResultSet = com.outworkers.phantom.ResultSet
  type Session = com.datastax.driver.core.Session
  type KeySpace = com.outworkers.phantom.connectors.KeySpace
  val KeySpace = com.outworkers.phantom.connectors.KeySpace
  type CassandraConnection = com.outworkers.phantom.connectors.CassandraConnection
  type RootConnector = com.outworkers.phantom.connectors.RootConnector
  val Analyzer = com.outworkers.phantom.builder.query.sasi.Analyzer
  val Mode = com.outworkers.phantom.builder.query.sasi.Mode
  type Analyzer[M <: Mode] = com.outworkers.phantom.builder.query.sasi.Analyzer[M]
  type SASIIndex[M <: Mode] = com.outworkers.phantom.keys.SASIIndex[M]
  type CustomIndex[M <: Mode] = SASIIndex[M]

  type StandardAnalyzer[M <: Mode] = com.outworkers.phantom.builder.query.sasi.Analyzer.StandardAnalyzer[M]
  type NonTokenizingAnalyzer[M <: Mode] = com.outworkers.phantom.builder.query.sasi.Analyzer.NonTokenizingAnalyzer[M]

  val Version = DefaultVersions

  type ListResult[R] = com.outworkers.phantom.builder.query.execution.ListResult[R]
  type IteratorResult[R] = com.outworkers.phantom.builder.query.execution.IteratorResult[R]
  type RecordResult[R] = com.outworkers.phantom.builder.query.execution.RecordResult[R]

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


  type KeySpaceDef = com.outworkers.phantom.connectors.CassandraConnection
  val ContactPoint = com.outworkers.phantom.connectors.ContactPoint
  val ContactPoints = com.outworkers.phantom.connectors.ContactPoints

  implicit def primitiveToTokenOp[RR : Primitive](value: RR): TokenConstructor[RR :: HNil, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(Primitive[RR].asCql(value)))
  }

  /**
    * Used as a secondary option when creating a [[ContactPoint]] to allow users to provide
    * a single [[KeySpace]] derived query. When users want to provide
    * a single argument to the [[ContactPoint#keySpace]] method, they can use
    * the following syntax to generate a full keyspace initialisation query.
    * The KeySpace will implicitly convert to a [[RootSerializer]].
    *
    * {{{
    *   KeySpace("test").ifNotExists
    * }}}
    */
  implicit def keyspaceToKeyspaceQuery(k: KeySpace): RootSerializer = new RootSerializer(k)

  implicit class SelectColumnRequired[
    Owner <: CassandraTable[Owner, Record],
    Record, T
  ](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col(r)
  }

  implicit class SelectColumnOptional[
    Owner <: CassandraTable[Owner, Record],
    Record, T
  ](col: OptionalColumn[Owner, Record, T]) extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col(r)
  }

  implicit class RichNumber(val percent: Int) {
    def percentile: CQLQuery = CQLQuery(percent.toString)
      .pad.append(CQLSyntax.CreateOptions.percentile)
  }

  implicit class PartitionTokenHelper[T](val col: AbstractColumn[T] with PartitionKey) {

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

}
