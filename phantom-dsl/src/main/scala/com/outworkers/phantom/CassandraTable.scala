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

import com.datastax.driver.core.{Row, Session}
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.DeleteClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.{RootCreateQuery, _}
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.macros.{NamingStrategy, TableHelper}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
 * Main representation of a Cassandra table.
 * @tparam T Type of this table.
 * @tparam R Type of record.
 */
abstract class CassandraTable[T <: CassandraTable[T, R], R](
  implicit helper: TableHelper[T, R]
) extends SelectTable[T, R] { self =>

  def columns: Seq[AbstractColumn[_]] = helper.fields(instance)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary).filterNot(_.isPartitionKey)

  def partitionKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPartitionKey)

  def clusteringColumns: Seq[AbstractColumn[_]] = columns.filter(_.isClusteringKey)

  def tableKey: String = helper.tableKey(instance)

  @deprecated("Method replaced with macro implementation", "2.0.0")
  def defineTableKey(): String = tableKey

  def instance: T = self.asInstanceOf[T]

  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  type ListColumn[RR] = com.outworkers.phantom.column.ListColumn[T, R, RR]
  type SetColumn[RR] =  com.outworkers.phantom.column.SetColumn[T, R, RR]
  type MapColumn[KK, VV] =  com.outworkers.phantom.column.MapColumn[T, R, KK, VV]
  type JsonColumn[RR] = com.outworkers.phantom.column.JsonColumn[T, R, RR]
  type OptionalJsonColumn[RR] = com.outworkers.phantom.column.OptionalJsonColumn[T, R, RR]
  type EnumColumn[RR <: Enumeration#Value] = com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]
  type OptionalEnumColumn[RR <: Enumeration#Value] = com.outworkers.phantom.column.OptionalPrimitiveColumn[T, R, RR]
  type JsonSetColumn[RR] = com.outworkers.phantom.column.JsonSetColumn[T, R, RR]
  type JsonListColumn[RR] = com.outworkers.phantom.column.JsonListColumn[T, R, RR]
  type JsonMapColumn[KK,VV] = com.outworkers.phantom.column.JsonMapColumn[T, R, KK, VV]
  type TupleColumn[RR] =  com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]
  type PrimitiveColumn[RR] = com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]
  type CustomColumn[RR] = com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]
  type Col[RR] = com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]

  def insertSchema()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): Unit = {
    Await.result(autocreate(keySpace).future(), 10.seconds)
  }

  def tableName(implicit strategy: NamingStrategy): String = strategy(helper.tableName)

  def fromRow(r: Row): R = helper.fromRow(instance, r)

  /**
   * The new create mechanism introduced in Phantom 1.6.0.
   * This uses the phantom proprietary QueryBuilder instead of the already available one in the underlying Java Driver.
   * @return A root create block, with full support for all CQL Create query options.
   */
  final def create: RootCreateQuery[T, R] = new RootCreateQuery(instance)

  def autocreate(keySpace: KeySpace): CreateQuery.Default[T, R] = create.ifNotExists()(keySpace)

  final def alter()(implicit keySpace: KeySpace): AlterQuery.Default[T, R] = AlterQuery(instance)

  final def alter[
    RR,
    NewType
  ](columnSelect: T => AbstractColumn[RR])(newType: Primitive[NewType])(implicit keySpace: KeySpace): AlterQuery.Default[T, RR] = {
    AlterQuery.alterType[T, RR, NewType](instance, columnSelect, newType)
  }

  final def alter[RR](
    columnSelect: T => AbstractColumn[RR],
    newName: String
  )(implicit keySpace: KeySpace): AlterQuery.Default[T, RR] = {
    AlterQuery.alterName[T, RR](instance, columnSelect, newName)
  }

  final def update()(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = UpdateQuery(instance)

  final def insert()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = InsertQuery(instance)

  //final def store()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = helper.store(instance)

  final def delete()(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery[T, R](instance)

  final def delete(
    conditions: (T => DeleteClause.Condition)*
  )(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    DeleteQuery[T, R](instance, conditions.map(_(instance).qb): _*)
  }

  final def truncate()(
    implicit keySpace: KeySpace
  ): TruncateQuery.Default[T, R] = TruncateQuery[T, R](instance)
}
