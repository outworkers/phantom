/*
 * Copyright 2011 - 2013 newzly ltd.
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
package net
package liftweb
package cassandra
package blackpepper
package query

import com.datastax.driver.core.querybuilder.Insert
import net.liftweb.cassandra.blackpepper.{ AbstractColumn, CassandraTable }

class InsertQuery[T <: CassandraTable[T, R], R](table: T, val qb: Insert) extends ExecutableStatement {

  def value[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, col.toCType(value)))
  }

  def valueOrNull[RR](c: T => AbstractColumn[RR], value: Option[RR]): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, value.map(col.toCType).orNull))
  }

}