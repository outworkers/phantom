/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom


import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.Try

import org.slf4j.LoggerFactory

import com.datastax.driver.core.{Session, Row}
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.twitter.util.{Duration, Await}

import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.query.{CreateQuery, DeleteQuery, InsertQuery, SelectCountQuery, TruncateQuery, UpdateQuery}

case class InvalidPrimaryKeyException(msg: String = "You need to define at least one PartitionKey for the schema") extends RuntimeException(msg)

abstract class CassandraTable[T <: CassandraTable[T, R], R] extends SelectTable[T, R] {

  private[this] lazy val _columns: MutableArrayBuffer[AbstractColumn[_]] = new MutableArrayBuffer[AbstractColumn[_]] with MutableSyncBuffer[AbstractColumn[_]]

  private[phantom] def insertSchema()(implicit session: Session) = Await.ready(create.execute(), Duration.fromSeconds(2))

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decoded
  }

  private[this] def extractCount(r: Row): Long = {
    Try { r.getLong("count") }.toOption.getOrElse(0L)
  }

  def columns: MutableArrayBuffer[AbstractColumn[_]] = _columns

  lazy val logger = {
    val klass = getClass.getName.stripSuffix("$")
    LoggerFactory.getLogger(klass)
  }

  def tableName: String = _name

  def fromRow(r: Row): R

  def update: UpdateQuery[T, R] = new UpdateQuery[T, R](this.asInstanceOf[T], QueryBuilder.update(tableName))

  def insert: InsertQuery[T, R] = new InsertQuery[T, R](this.asInstanceOf[T], QueryBuilder.insertInto(tableName))

  def delete: DeleteQuery[T, R] = new DeleteQuery[T, R](this.asInstanceOf[T], QueryBuilder.delete.from(tableName))

  def create: CreateQuery[T, R] = new CreateQuery[T, R](this.asInstanceOf[T], "")

  def truncate: TruncateQuery[T, R] = new TruncateQuery[T, R](this.asInstanceOf[T], QueryBuilder.truncate(tableName))

  def count: SelectCountQuery[T, Long] = new SelectCountQuery[T, Long](this.asInstanceOf[T], QueryBuilder.select().countAll().from(tableName), extractCount)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary).filterNot(_.isPartitionKey)

  def partitionKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPartitionKey)

  def clusteringColumns: Seq[AbstractColumn[_]] = columns.filter(_.isClusteringKey)

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
      throw InvalidPrimaryKeyException()
    } else if (operand == 0) {
      if (primaries.isEmpty) {
        s"${partitions.head.name}"
      } else {
        s"${partitions.head.name}, $primaryString"
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
      logger.error("When using CLUSTERING ORDER all PrimaryKey definitions must become a ClusteringKey definition and specify order.")
      throw new InvalidPrimaryKeyException("When using CLUSTERING ORDER all PrimaryKey definitions must become a ClusteringKey definition and specify order.")
    }
  }

  @throws[InvalidPrimaryKeyException]
  def schema(): String = {
    preconditions()

    val queryInit = s"CREATE TABLE IF NOT EXISTS $tableName ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      if (c.isStaticColumn) {
        s"$qb, ${c.name} ${c.cassandraType} static"
      } else {
        s"$qb, ${c.name} ${c.cassandraType}"
      }
    })
    val tableKey = defineTableKey()
    logger.info(s"Adding Primary keys indexes: $tableKey")
    val queryPrimaryKey  = if (tableKey.length > 0) s", $tableKey" else ""

    val query = queryInit + queryColumns.drop(1) + queryPrimaryKey + ")"
    val finalQuery = query + clusteringKey
    if (finalQuery.last != ';') finalQuery + ";" else finalQuery
  }

  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => {
      val query = s"CREATE INDEX IF NOT EXISTS ${tableName}_${k.name} ON $tableName (${k.name});"
      logger.info("Auto-generating CQL queries for secondary indexes")
      logger.info(query)
      query
    })
  }

  Lock.synchronized {
    val instanceMirror = cm.reflect(this)
    val selfType = instanceMirror.symbol.toType

    // Collect all column definitions starting from base class
    val columnMembers = MutableArrayBuffer.empty[Symbol]
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[AbstractColumn[_]])
        baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val column = instanceMirror.reflectModule(symbol.asModule).instance
        _columns += column.asInstanceOf[AbstractColumn[_]]
    }
  }
}




private[phantom] case object Lock
