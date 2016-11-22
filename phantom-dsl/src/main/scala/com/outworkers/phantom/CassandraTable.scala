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
import com.outworkers.phantom.builder.clauses.DeleteClause
import com.outworkers.phantom.builder.query.{RootCreateQuery, _}
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.exceptions.{InvalidClusteringKeyException, InvalidPrimaryKeyException}
import com.outworkers.phantom.macros.TableHelper
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
 * Main representation of a Cassandra table.
 * @tparam T Type of this table.
 * @tparam R Type of record.
 */
abstract class CassandraTable[T <: CassandraTable[T, R], R](
  implicit helper: TableHelper[T, R]
) extends SelectTable[T, R] { self: CassandraTable[T, R] =>

  def columns: Set[AbstractColumn[_]] = helper.fields(this)

  def secondaryKeys: Set[AbstractColumn[_]] = {
    columns.filter(_.isSecondaryKey)
  }

  def primaryKeys: Set[AbstractColumn[_]] = {
    columns.filter(_.isPrimary).filterNot(_.isPartitionKey)
  }

  def partitionKeys: Set[AbstractColumn[_]] = {
    columns.filter(_.isPartitionKey)
  }

  def clusteringColumns: Set[AbstractColumn[_]] = {
    columns.filter(_.isClusteringKey)
  }

  protected[this] def instance: T = this.asInstanceOf[T]

  type ListColumn[RR] = com.outworkers.phantom.column.ListColumn[T, R, RR]
  type SetColumn[RR] =  com.outworkers.phantom.column.SetColumn[T, R, RR]
  type MapColumn[KK, VV] =  com.outworkers.phantom.column.MapColumn[T, R, KK, VV]
  type JsonColumn[RR] = com.outworkers.phantom.column.JsonColumn[T, R, RR]
  type EnumColumn[RR <: Enumeration#Value] = com.outworkers.phantom.column.PrimitiveColumn[T, R, RR]
  type OptionalEnumColumn[RR <: Enumeration#Value] = com.outworkers.phantom.column.OptionalPrimitiveColumn[T, R, RR]
  type JsonSetColumn[RR] = com.outworkers.phantom.column.JsonSetColumn[T, R, RR]
  type JsonListColumn[RR] = com.outworkers.phantom.column.JsonListColumn[T, R, RR]
  type JsonMapColumn[KK,VV] = com.outworkers.phantom.column.JsonMapColumn[T, R, KK, VV]

  def insertSchema()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): Unit = {
    Await.result(autocreate(keySpace).future(), 10.seconds)
  }

  lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def tableName: String = helper.tableName

  def fromRow(r: Row): R

  /**
   * The new create mechanism introduced in Phantom 1.6.0.
   * This uses the phantom proprietary QueryBuilder instead of the already available one in the underlying Java Driver.
   * @return A root create block, with full support for all CQL Create query options.
   */
  final def create: RootCreateQuery[T, R] = new RootCreateQuery(instance)

  def autocreate(keySpace: KeySpace): CreateQuery.Default[T, R] = create.ifNotExists()(keySpace)

  final def alter()(implicit keySpace: KeySpace): AlterQuery.Default[T, R] = AlterQuery(instance)

  final def update()(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = UpdateQuery(instance)

  final def insert()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = InsertQuery(instance)

  final def delete()(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery[T, R](instance)

  final def delete(
    conditions: (T => DeleteClause.Condition)*
  )(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    DeleteQuery[T, R](
      instance,
      conditions.map(_(instance).qb): _*
    )
  }

  final def truncate()(
    implicit keySpace: KeySpace
  ): TruncateQuery.Default[T, R] = TruncateQuery[T, R](instance)

  def clustered: Boolean = clusteringColumns.nonEmpty

  /**
    * This method will filter the columns from a Clustering Order definition.
    * It is used to define TimeSeries tables, using the ClusteringOrder trait
    * combined with a directional trait, either Ascending or Descending.
    *
    * This method will simply add to the trailing of a query.
    * @return The clustering key, defined as a string or the empty string.
    */
  private[phantom] def clusteringKey: String = {
    if (clusteringColumns.nonEmpty) {
      val key = clusteringColumns.map(col => {
        val direction = if (col.isAscending) {
          "ASC"
        } else {
          "DESC"
        }
        s"${col.name} $direction"
      })
      s"WITH CLUSTERING ORDER BY (${key.mkString(", ")})"
    } else {
      ""
    }
  }

  /**
    * This method will define the PRIMARY_KEY of the table.
    * <ul>
    *   <li>
    *    For more than one partition key, it will define a Composite Key.
    *    Example: PRIMARY_KEY((partition_key_1, partition_key2), primary_key_1, etc..)
    *   </li>
    *   <li>
    *     For a single partition key, it will define a Compound Key.
    *     Example: PRIMARY_KEY(partition_key_1, primary_key_1, primary_key_2)
    *   </li>
    *   <li>
    *     For no partition key, it will throw an exception.
    *   </li>
    * </ul>
    * @return A string value representing the primary key of the table.
    */
  @throws(classOf[InvalidPrimaryKeyException])
  private[phantom] def defineTableKey(): String = {

    preconditions()

    // Get the list of primary keys that are not partition keys.
    val primaries = primaryKeys
    val primaryString = primaryKeys.map(_.name).mkString(", ")

    // Get the list of partition keys that are not primary keys
    // This is done to avoid including the same columns twice.
    val partitions = partitionKeys.toList
    val partitionString = s"(${partitions.map(_.name).mkString(", ")})"

    val operand = partitions.lengthCompare(1)
    val key = if (operand < 0) {
      throw InvalidPrimaryKeyException(tableName)
    } else if (operand == 0) {

      val partitionKey = partitions.headOption.map(_.name).orNull

      if (primaries.isEmpty) {
        partitionKey
      } else {
        s"$partitionKey, $primaryString"
      }
    } else {
      if (primaries.isEmpty) {
        partitionString
      } else {
        s"$partitionString, $primaryString"
      }
    }
    s"PRIMARY KEY ($key)"
  }

  /**
    * This method will check for common Cassandra anti-patterns during the intialisation of a schema.
    * If the Schema definition violates valid CQL standard, this function will throw an error.
    *
    * A perfect example is using a mixture of Primary keys and Clustering keys in the same schema.
    * While a Clustering key is also a primary key, when defining a clustering key all other keys must become clustering keys and specify their order.
    *
    * We could auto-generate this order but we wouldn't be making false assumptions about the desired ordering.
    */
  private[this] def preconditions(): Unit = {
    if (clustered && primaryKeys.diff(clusteringColumns).nonEmpty) {
      logger.error("When using CLUSTERING ORDER all PrimaryKey definitions" +
        " must become a ClusteringKey definition and specify order.")
      throw InvalidClusteringKeyException(tableName)
    }
  }
}
