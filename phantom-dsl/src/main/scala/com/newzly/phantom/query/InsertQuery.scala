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
package com.newzly.phantom.query

import com.datastax.driver.core.querybuilder.{ Insert, QueryBuilder }
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.AbstractColumn
import com.twitter.util.Duration

class InsertQuery[T <: CassandraTable[T, R], R](table: T, val qb: Insert) extends ExecutableStatement {

  def value[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    qb.value(col.name, col.toCType(value))
    this
  }

  def valueOrNull[RR](c: T => AbstractColumn[RR], value: Option[RR]): InsertQuery[T, R] = {
    val col = c(table)
    qb.value(col.name, value.map(col.toCType).orNull)
    this
  }

  /**
   * Sets a timestamp expiry for the inserted column.
   * This value is set in seconds.
   * @param expiry The expiry time.
   * @return
   */
  final def ttl(expiry: Duration): InsertQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry.inSeconds))
    this
  }

  override def toString: String = {
    val query = s"${qb.getQueryString}(${qb.getValues}: ${qb.toString})}"
    table.logger.info(s"$query")
    query
  }
}