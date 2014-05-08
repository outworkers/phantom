/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom

import scala.collection.mutable.{ ListBuffer, ArrayBuffer }
import scala.util.Try
import org.slf4j.LoggerFactory
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder
import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.{ currentMirror => cm }
import com.newzly.phantom.column.AbstractColumn
import com.newzly.phantom.query.{
  CreateQuery,
  DeleteQuery,
  InsertQuery,
  SelectQuery,
  TruncateQuery,
  UpdateQuery
}

case class FieldHolder(name: String, metaField: AbstractColumn[_])
case class InvalidPrimaryKeyException(msg: String = "You need to define at least one PartitionKey for the schema") extends RuntimeException(msg)

abstract class CassandraTable[T <: CassandraTable[T, R], R] extends SelectTable[T, R] {

  val order = new ArrayBuffer[String] with collection.mutable.SynchronizedBuffer[String]

  def runSafe[A](f : => A) : A = {
    Safe.runSafe(System.identityHashCode(this))(f)
  }

  private[this] lazy val _columns: ArrayBuffer[AbstractColumn[_]] = new ArrayBuffer[AbstractColumn[_]] with collection.mutable.SynchronizedBuffer[AbstractColumn[_]]

  final def addColumn(column: AbstractColumn[_]): Unit = {
    _columns += column
  }

  def columns: ArrayBuffer[AbstractColumn[_]] = _columns

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  def extractCount(r: Row): Option[Long] = {
    Try { r.getLong("count") }.toOption
  }

  lazy val logger = LoggerFactory.getLogger(tableName)

  def tableName: String = _name

  def fromRow(r: Row): R

  def update = new UpdateQuery[T, R](this.asInstanceOf[T], QueryBuilder.update(tableName))

  def insert = new InsertQuery[T, R](this.asInstanceOf[T], QueryBuilder.insertInto(tableName))

  def delete = new DeleteQuery[T, R](this.asInstanceOf[T], QueryBuilder.delete.from(tableName))

  def create = new CreateQuery[T, R](this.asInstanceOf[T], "")

  def truncate = new TruncateQuery[T, R](this.asInstanceOf[T], QueryBuilder.truncate(tableName))

  def count = new SelectQuery[T, Option[Long]](this.asInstanceOf[T], QueryBuilder.select().countAll().from(tableName), extractCount)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary)

  def partitionKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPartitionKey)

  private[phantom] def clusterOrderSchema(query: String): String = {
    if (columns.count(_.isClusteringKey) == 1) {
      val clusteringColumn = columns.filter(_.isClusteringKey).head
      val direction = if (clusteringColumn.isAscending) "ASC" else "DESC"
      s"$query WITH CLUSTERING ORDER BY (${clusteringColumn.name} $direction);"
    } else {
      query
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

    // Get the list of primary keys that are not partition keys.
    val primaries = primaryKeys.filterNot(_.isPartitionKey)
    val primaryString = primaries.map(_.name).mkString("")

    // Get the list of partition keys that are not primary keys
    // This is done to avoid including the same columns twice.
    val partitions = primaryKeys.filter(_.isPartitionKey).toList

    val partitionString = s"(${partitions.map(_.name).mkString(", ")})"

    val key = partitions match {
      case head :: tail if !tail.isEmpty =>
        if (primaries.isEmpty)
          partitionString
        else
          s"$partitionString, $primaryString"
      case head :: tail =>
        if(primaries.isEmpty)
          s"${head.name}"
        else
          s"${head.name}, $primaryString"
      case Nil =>  throw InvalidPrimaryKeyException()
    }

    s"PRIMARY_KEY ($key)"
  }

  def schema(): String = {
    val queryInit = s"CREATE TABLE IF NOT EXISTS $tableName ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      if (c.isStaticColumn) {
        s"$qb, ${c.name} ${c.cassandraType} static"
      } else {
        s"$qb, ${c.name} ${c.cassandraType}"
      }
    })
    val tableKey = defineTableKey()
    logger.info(s"Adding Primary keys indexes: $tableKey}")
    val queryPrimaryKey  = if (tableKey.length > 0) s", ${defineTableKey()}" else ""

    val query = queryInit + queryColumns.drop(1) + queryPrimaryKey + ")"
    val finalQuery = clusterOrderSchema(query)
    if (finalQuery.last != ';') finalQuery + ";" else finalQuery
  }

  /**
   * This creates a sequence of string queries to execute for creating secondary indexes.
   * The queries are then processed with table.create().
   * As secondary indexes cannot be directly specified in the schema,
   * for each of them a separate Cassandra query and Future will be executed.
   * @return The sequence of secondary index creation queries.
   */
  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => {
      val query = s"CREATE INDEX IF NOT EXISTS ${k.name} ON $tableName (${k.name});"
      logger.info(query)
      query
    })
  }

  private[this] def introspect(f: (String, AbstractColumn[_]) => Any): Unit = {
    val im = cm.reflect(this)
    val members = im.symbol.selfType.members
    val objectMembers = members.filter(_.isModule).map(_.asModule)
    val metaDataMembers = objectMembers.filter(_.moduleClass.asClass.toType.<:<(ru.typeOf[AbstractColumn[_]]))
    metaDataMembers.foreach { m =>
      f(m.name.decoded, im.reflectModule(m).instance.asInstanceOf[AbstractColumn[_]])
    }
  }

  introspect {
    case (name, ac) => _columns += ac
  }
}
