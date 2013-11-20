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
package net
package liftweb
package cassandra
package blackpepper
package query

import com.datastax.driver.core.querybuilder.{ Clause, Select }
import com.datastax.driver.core.Row

import net.liftweb.cassandra.blackpepper.{ CassandraTable }

class SelectQuery[T <: CassandraTable[T, _], R](val table: T, val qb: Select, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def where(c: T => Clause) = {
    new SelectWhere[T, R](table, qb.where(c(table)), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }
}

class SelectWhere[T <: CassandraTable[T, _], R](val table: T, val qb: Select.Where, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def where(c: T => Clause) = {
    new SelectWhere[T, R](table, qb.and(c(table)), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

  def and = where _

}