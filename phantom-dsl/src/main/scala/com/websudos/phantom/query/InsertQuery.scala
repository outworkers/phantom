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
package com.websudos.phantom.query

import scala.util.Try
import org.joda.time.DateTime

import com.datastax.driver.core.querybuilder.{ Insert, QueryBuilder }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.AbstractColumn

class InsertQuery[T <: CassandraTable[T, R], R](table: T, val qb: Insert) extends CQLQuery[InsertQuery[T, R]] with BatchableQuery[InsertQuery[T, R]] {

  final def value[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, col.toCType(value)))
  }

  final def valueOrNull[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, Try {
      col.toCType(value)
    } getOrElse null.asInstanceOf[T]))
  }

  final def ifNotExists[RR](): InsertQuery[T, R] = {
    new InsertQuery[T, R](table, qb.ifNotExists())
  }

  /**
   * Sets a timestamp expiry for the inserted column.
   * This value is set in seconds.
   * @param expiry The expiry time.
   * @return
   */
  def ttl(expiry: Int): InsertQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry))
    this
  }

  def timestamp(l: Long): InsertQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }

  def timestamp(time: DateTime): InsertQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(time.getMillis))
    this
  }
}
