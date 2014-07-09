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

import com.datastax.driver.core.querybuilder.{QueryBuilder, Delete}
import com.websudos.phantom.CassandraTable

class DeleteQuery[T <: CassandraTable[T, R], R](table: T, val qb: Delete)
  extends CQLQuery[DeleteQuery[T, R]] with BatchableQuery[DeleteQuery[T, R]] {

  def where[RR](condition: T => QueryCondition): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.where(condition(table).clause))
  }

  def timestamp(l: Long): DeleteQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }
}

class DeleteWhere[T <: CassandraTable[T, R], R](table: T, val qb: Delete.Where)
  extends CQLQuery[DeleteWhere[T, R]] with BatchableQuery[DeleteWhere[T, R]] {

  def and[RR](condition: T => QueryCondition): DeleteWhere[T, R] = {
    new DeleteWhere[T, R](table, qb.and(condition(table).clause))
  }

  def timestamp(l: Long): DeleteWhere[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }
}
