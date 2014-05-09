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

import scala.collection.mutable.ArrayBuffer
import scala.util.Try
import org.slf4j.LoggerFactory
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder
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

abstract class CassandraTable[T <: CassandraTable[T, R], R] extends SelectTable[T, R] {

  def runSafe[A](f : => A) : A = {
    Safe.runSafe(System.identityHashCode(this))(f)
  }

  private[this] lazy val _columns: ArrayBuffer[AbstractColumn[_]] =
    new ArrayBuffer[AbstractColumn[_]] with collection.mutable.SynchronizedBuffer[AbstractColumn[_]]

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

  private[phantom] def clusterOrderSchema(query: String): String = {
    if (columns.count(_.isClusteringKey) == 1) {
      val clusteringColumn = columns.filter(_.isClusteringKey).head
      val direction = if (clusteringColumn.isAscending) "ASC" else "DESC"
      s"$query WITH CLUSTERING ORDER BY (${clusteringColumn.name} $direction);"
    } else {
      query
    }
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
    val primaryKeysString = primaryKeys.filterNot(_.isPartitionKey).map(_.name).mkString(", ")
    val pkes = {
      primaryKeys.toList.filter(_.isPartitionKey) match {
        case head :: tail if !tail.isEmpty => throw new Exception("only one partition key is allowed in the schema")
        case head :: tail =>
          if(primaryKeysString.isEmpty)
            s"${head.name}"
          else
            s"${head.name}, $primaryKeysString"
        case Nil =>  throw new Exception("please specify the partition key for the schema")
      }
    }
    logger.info(s"Adding Primary keys indexes: $pkes")
    val queryPrimaryKey  = if (pkes.length > 0) s", PRIMARY KEY ($pkes)" else ""

    val query = queryInit + queryColumns.drop(1) + queryPrimaryKey + ")"
    val finalQuery = clusterOrderSchema(query)
    if (finalQuery.last != ';') finalQuery + ";" else finalQuery
  }

  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => {
      val query = s"CREATE INDEX IF NOT EXISTS ${k.name} ON $tableName (${k.name});"
      logger.info(query)
      query
    })
  }

  this.runSafe {
    import reflect.runtime.universe._

    val mirror =  scala.reflect.runtime.currentMirror
    val instanceMirror = mirror.reflect(this)

    val selfType = instanceMirror.symbol.toType

    // Collect all column definitions starting from base class
    val columnMembers = scala.collection.mutable.ArrayBuffer.empty[Symbol]
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])
        baseClassColumns.foreach(symbol => if (!columnMembers.exists(_ == symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val column = instanceMirror.reflectModule(symbol.asModule).instance
        _columns += column.asInstanceOf[AbstractColumn[_]]
    }
  }
}
