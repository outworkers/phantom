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

import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Update, Using }
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.AbstractColumn

class AssignmentsQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Assignments) extends ExecutableStatement {

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.and(a(table)))
  }

  def and = modify _
}

class AssignmentOptionQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Options) extends ExecutableStatement {

  def ttl(seconds: Int): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(QueryBuilder.ttl(seconds)))
  }

  def using(u: Using): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(u))
  }
}

class UpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update) {

  def where[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.where(condition(table).clause))
  }

  /**
   * Sets a timestamp expiry for the inserted column.
   * This value is set in seconds.
   * @param expiry The expiry time.
   * @return
   */
  def ttl(expiry: Int): UpdateQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry))
    this
  }

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }
}

class UpdateWhere[T <: CassandraTable[T, R], R](table: T, val qb: Update.Where) {

  def where[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.and(condition(table).clause))
  }

  def and = where _

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }
}

