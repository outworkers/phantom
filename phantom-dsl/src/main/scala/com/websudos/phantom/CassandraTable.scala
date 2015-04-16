/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom

import com.datastax.driver.core.{Row, Session}
import com.websudos.phantom.builder.query._
import com.websudos.phantom.builder.query.prepared.PreparedBuilder
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.exceptions.InvalidPrimaryKeyException
import org.slf4j.LoggerFactory

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer}
import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}

abstract class CassandraTable[T <: CassandraTable[T, R], R] extends SelectTable[T, R] {

  private[phantom] def insertSchema()(implicit session: Session, keySpace: KeySpace): Unit = {
    Await.ready(create.ifNotExists().future(), 3.seconds)
  }

  private[phantom] def self: T = this.asInstanceOf[T]

  private[this] lazy val _columns: MutableArrayBuffer[AbstractColumn[_]] = new MutableArrayBuffer[AbstractColumn[_]]

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decodedName.toString
  }

  def columns: MutableArrayBuffer[AbstractColumn[_]] = _columns

  lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def tableName: String = _name

  def fromRow(r: Row): R

  /**
   * The new create mechanism introduced in Phantom 1.6.0.
   * This uses the phantom proprietary QueryBuilder instead of the already available one in the underlying Java Driver.
   * @return A root create block, with full support for all CQL Create query options.
   */
  final def create: RootCreateQuery[T, R] = new RootCreateQuery(this.asInstanceOf[T])

  final def alter()(implicit keySpace: KeySpace): AlterQuery.Default[T, R] = AlterQuery(this.asInstanceOf[T])

  final def update()(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = UpdateQuery(this.asInstanceOf[T])

  final def insert()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = InsertQuery(this.asInstanceOf[T])

  final def delete()(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery[T, R](this.asInstanceOf[T])

  final def delete(clause: T => AbstractColumn[_])(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery[T, R](this.asInstanceOf[T], clause(this.asInstanceOf[T]).name)

  final def truncate()(implicit keySpace: KeySpace): TruncateQuery.Default[T, R] = TruncateQuery[T, R](this.asInstanceOf[T])

  final def automigrate()(implicit session: Session, keySpace: KeySpace, ec: ExecutionContext): ExecutableStatementList = {
    SchemaAutoDiffer.automigrate(this.asInstanceOf[T])
  }

  def prepare()(implicit keySpace: KeySpace): PreparedBuilder[T, R] = new PreparedBuilder[T, R](this.asInstanceOf[T])

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

  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => {
      val query = s"CREATE INDEX IF NOT EXISTS ${tableName}_${k.name} ON $tableName (${k.name});"
      logger.info("Auto-generating CQL queries for secondary indexes")
      logger.info(query)
      query
    })
  }

  Lock.synchronized {

    Manager.addTable(this)

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
