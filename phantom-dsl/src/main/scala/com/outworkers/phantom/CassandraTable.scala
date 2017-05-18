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

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.clauses.DeleteClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.{RootCreateQuery, _}
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.macros.TableHelper
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
 * Main representation of a Cassandra table.
 * @tparam T Type of this table.
 * @tparam R Type of record.
 */
abstract class CassandraTable[T <: CassandraTable[T, R], R](
  implicit val helper: TableHelper[T, R]
) extends SelectTable[T, R] { self =>

  def columns: Seq[AbstractColumn[_]] = helper.fields(instance)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary).filterNot(_.isPartitionKey)

  def partitionKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPartitionKey)

  def clusteringColumns: Seq[AbstractColumn[_]] = columns.filter(_.isClusteringKey)

  def tableKey: String = helper.tableKey(instance)

  def instance: T = self.asInstanceOf[T]

  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def insertSchema()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): Unit = Await.result(autocreate(keySpace).future(), 10.seconds)

  def tableName: String = helper.tableName

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
  ](columnSelect: T => AbstractColumn[RR])(newType: Primitive[NewType])(
    implicit keySpace: KeySpace
  ): AlterQuery.Default[T, RR] = {
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

  /**
    * Automatically generated store method for the record type.
    * @param input The input which will be auto-tupled and compared.
    * @param keySpace The keyspace in which the query will be executed.
    * @tparam V1 The type of the input.
    * @return A default input query.
    */
  def store[V1](input: V1)(
    implicit keySpace: KeySpace
  ): InsertQuery.Default[T, R] = helper.store(instance, input.asInstanceOf[helper.Repr])

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
