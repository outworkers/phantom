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

import com.datastax.driver.core.querybuilder.{ Clause, Delete }
import net.liftweb.cassandra.blackpepper.{ CassandraTable }

class DeleteQuery[T <: CassandraTable[T, R], R](table: T, val qb: Delete) extends ExecutableStatement {

  def where(c: T => Clause): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.where(c(table)))
  }
}

class DeleteWhere[T <: CassandraTable[T, R], R](table: T, val qb: Delete.Where) extends ExecutableStatement {

  def where(c: T => Clause): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.and(c(table)))
  }

  def and(c: T => Clause) = where(c)

}