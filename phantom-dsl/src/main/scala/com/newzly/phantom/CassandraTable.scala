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

import java.lang.reflect.Method
import scala.collection.mutable.{ ListBuffer, ArrayBuffer }
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

  def truncate = new TruncateQuery[T, R](this.asInstanceOf[T], QueryBuilder.truncate(tableName))

  def count = new SelectQuery[T, Option[Long]](this.asInstanceOf[T], QueryBuilder.select().countAll().from(tableName), extractCount)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary)

  def schema(): String = {
    val queryInit = s"CREATE TABLE IF NOT EXISTS $tableName ("
    val queryColumns = columns.foldLeft("")((qb, c) => {
      if (c.isStaticColumn) {
        val q = s"$qb, ${c.name} ${c.cassandraType} static"
        Console.println(q)
        q
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
    if (query.last != ';') query + ";" else query
  }

  def createIndexes(): Seq[String] = {
    secondaryKeys.map(k => s"CREATE INDEX IF NOT EXISTS ${k.name} ON $tableName (${k.name});")
  }

  private[this] def isField(m: Method) = {
    val ret = !m.isSynthetic && classOf[AbstractColumn[_]].isAssignableFrom(m.getReturnType)
    ret
  }

  private[this] def introspect(rec: CassandraTable[T, R], methods: Array[Method])(f: (Method, AbstractColumn[_]) => Any): Unit = {

    // find all the potential fields
    val potentialFields = methods.filter(isField)

    // any fields with duplicate names get put into a List
    val fullMap = potentialFields.foldLeft[Map[String, List[Method]]](Map()) {
      case (map, method) => val name = method.getName
        order += method.getName
        map + (name -> (method :: map.getOrElse(name, Nil)))

    }

    // sort each list based on having the most specific type and use that method
    val realMeth = fullMap.values.map(_.sortWith {
      case (a, b) => !a.getReturnType.isAssignableFrom(b.getReturnType)
    }).map(_.head)

    for (v <- realMeth) {
      v.invoke(rec) match {
        case mf: AbstractColumn[_]  => f(v, mf)
        case _ =>
      }
    }

  }

  this.runSafe {
    val tArray = new ListBuffer[FieldHolder]
    introspect(this, this.getClass.getSuperclass.getMethods) {
      case (v, mf) =>
        tArray += FieldHolder(mf.name, mf)
    }

  val sorted = tArray.sortWith((field1, field2) => order.indexWhere(field1.name == _ ) < order.indexWhere(field2.name == _ ))
    sorted.foreach(_columns += _.metaField)
  }
}
