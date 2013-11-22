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
package com
package newzly
package cassandra
package phantom
package query

import com.datastax.driver.core.querybuilder.{ Assignment, Clause, Update }
import com.newzly.cassandra.phantom.{ CassandraTable => CassandraTable }

class UpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update) {

  def where(c: T => Clause): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.where(c(table)))
  }

}

class UpdateWhere[T <: CassandraTable[T, R], R](table: T, val qb: Update.Where) {

  def where(c: T => Clause): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.and(c(table)))
  }

  def and = where _

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }

}

class AssignmentsQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Assignments) extends ExecutableStatement {

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.and(a(table)))
  }

  def and = modify _

}