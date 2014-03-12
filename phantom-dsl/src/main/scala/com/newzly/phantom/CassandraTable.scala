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

import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.JavaConverters._
import scala.collection.mutable.{ ArrayBuffer, SynchronizedBuffer }
import scala.util.control.NonFatal
import scala.util.Try
import org.slf4j.LoggerFactory
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.QueryBuilder

import com.newzly.phantom.query.{ CreateQuery, DeleteQuery, InsertQuery, SelectQuery, UpdateQuery }
import com.newzly.phantom.column.AbstractColumn
import scala.reflect.runtime.universe._


abstract class CassandraTable[T <: CassandraTable[T, R]  : TypeTag, R] extends SelectTable[T, R] {

  private[this] val greedyInit = new AtomicBoolean(false)

  private[this] lazy val _columns: ArrayBuffer[AbstractColumn[_]] = new ArrayBuffer[AbstractColumn[_]] with SynchronizedBuffer[AbstractColumn[_]]

  def addColumn(column: AbstractColumn[_]): Unit = {
    _columns += column
  }

  def columns: Seq[AbstractColumn[_]] = _columns.toSeq.reverse

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  def extractCount(r: Row): Option[Long] = {
    Try {
      Some(r.getLong("count"))
    } getOrElse None
  }

  lazy val logger = LoggerFactory.getLogger(tableName)

  def tableName: String = _name

  def fromRow(r: Row): R

  def update = new UpdateQuery[T, R](this.asInstanceOf[T], QueryBuilder.update(tableName))

  def insert = new InsertQuery[T, R](this.asInstanceOf[T], QueryBuilder.insertInto(tableName))

  def delete = new DeleteQuery[T, R](this.asInstanceOf[T], QueryBuilder.delete.from(tableName))

  def create = new CreateQuery[T, R](this.asInstanceOf[T], "")

  def count = new SelectQuery[T, Option[Long]](this.asInstanceOf[T], QueryBuilder.select().countAll().from(tableName), extractCount)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary)

  def schema(): String = {
    val queryInit = s"CREATE TABLE $tableName ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      s"$qb, ${c.name} ${c.cassandraType}"
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
    if (query.last != ';') query + ";" else query
  }

  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => s"CREATE IF NOT EXISTS INDEX ${k.name} ON $tableName (${k.name});")
  }

  val mirror = runtimeMirror(getClass.getClassLoader)
  val reflection  = mirror.reflect(this)

  if (greedyInit.compareAndSet(false, true)) {
    Console.println("Initialising tables")
    synchronized {
      typeTag[T].tpe.members.filter(_.isModule).foreach(m => try {
        reflection.reflectModule(m.asModule).instance
      } catch {
        case NonFatal(err) => logger.error(err.getMessage)
      })
    }
  }
}
