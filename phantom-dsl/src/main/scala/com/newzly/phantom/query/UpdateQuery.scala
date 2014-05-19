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

class AssignmentsQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Assignments)
  extends SharedQueryMethods[AssignmentsQuery[T, R], Update.Assignments](qb) {


  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.and(a(table)))
  }

  def and = modify _
}

class AssignmentOptionQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Options)
  extends SharedQueryMethods[AssignmentOptionQuery[T, R], Update.Options](qb) {

  def ttl(seconds: Int): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(QueryBuilder.ttl(seconds)))
  }

  def using(u: Using): AssignmentOptionQuery[T, R] = {
    new AssignmentOptionQuery[T, R](table, qb.and(u))
  }
}

class ConditionalUpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Conditions)
  extends SharedQueryMethods[ConditionalUpdateQuery[T, R], Update.Conditions](qb) {

  def where[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.where(condition(table).clause))
  }
}

class ConditionalUpdateWhereQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update.Conditions)
  extends SharedQueryMethods[ConditionalUpdateWhereQuery[T, R], Update.Conditions](qb) {
}

class UpdateQuery[T <: CassandraTable[T, R], R](table: T, val qb: Update)
  extends SharedQueryMethods[UpdateQuery[T, R], Update](qb) with ExecutableStatement {

  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

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

class UpdateWhere[T <: CassandraTable[T, R], R](table: T, val qb: Update.Where)
  extends SharedQueryMethods[UpdateWhere[T, R], Update.Where](qb) {

  def where[RR](condition: T => QueryCondition): UpdateWhere[T, R] = {
    new UpdateWhere[T, R](table, qb.and(condition(table).clause))
  }

  def onlyIf[RR](condition: T => SecondaryQueryCondition): ConditionalUpdateQuery[T, R] = {
    new ConditionalUpdateQuery[T, R](table, qb.onlyIf(condition(table).clause))
  }

  def and = where _

  def modify(a: T => Assignment): AssignmentsQuery[T, R] = {
    new AssignmentsQuery[T, R](table, qb.`with`(a(table)))
  }
}

