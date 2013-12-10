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

import com.datastax.driver.core.querybuilder.{QueryBuilder, Assignment, Clause, Update}
import com.newzly.cassandra.phantom.{ CassandraTable => CassandraTable }
import com.newzly.cassandra.phantom.query.AssignmentsQuery

class UpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update) {
  def where[RR](c: T => AbstractColumn[RR], value: RR, operator: (T => AbstractColumn[RR],RR)=>T=>Clause): UpdateWhere[T, R] = {
    val clause = operator(c,value.asInstanceOf[RR])(table)
    new UpdateWhere[T, R](table, qb.where(clause))
  }
}

class UpdateWhere[T <: CassandraTable[T, R], R](table: T, val qb: Update.Where) {

  def where[RR](c: T => AbstractColumn[RR], value: RR, operator: (T => AbstractColumn[RR],RR)=>T=>Clause): UpdateWhere[T, R] = {
    val clause = operator(c,value)(table)
    new UpdateWhere[T, R](table, qb.and(clause))
  }

  def and = where _

  def modify[RR](c: T => AbstractColumn[RR], value: RR): AssignmentsQuery[T, R] = {
    val col = c(table)
    val a = QueryBuilder.set(col.name, col.toCType(value))
    new AssignmentsQuery[T, R](table, qb.`with`(a))
  }
}

class AssignmentsQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Assignments) extends ExecutableStatement {

  def modify[RR](c: T => AbstractColumn[RR], value: RR): AssignmentsQuery[T, R] = {
    val col = c(table)
    val a = QueryBuilder.set(col.name, col.toCType(value))
    new AssignmentsQuery[T, R](table, qb.and(a))
  }
  def and = modify _

}