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

import scala.concurrent.Future
import com.datastax.driver.core.{Session, Row}
import com.datastax.driver.core.querybuilder.Select
import com.newzly.phantom.CassandraTable
import play.api.libs.iteratee.{ Iteratee => PlayIteratee }

class SelectQuery[T <: CassandraTable[T, _], R](val table: T, val qb: Select, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def setFetchSize(n: Int) = {
    qb.setFetchSize(n)
    this
  }

  def allowFiltering() : SelectQuery[T, R] = {
    new SelectQuery(table, qb.allowFiltering(), fromRow)
  }

  def where[RR](condition: T => QueryCondition): SelectWhere[T, R] = {
    new SelectWhere[T, R](table, qb.where(condition(table).clause), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    new SelectQuery[T, R](table, qb.limit(1), fromRow).fetchEnumerator flatMap(_ run PlayIteratee.head)
  }
}

class SelectWhere[T <: CassandraTable[T, _], R](val table: T, val qb: Select.Where, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    new SelectQuery[T, R](table, qb.limit(1), fromRow).fetchEnumerator flatMap(_ run PlayIteratee.head)
  }

  def where[RR](condition: T => QueryCondition): SelectWhere[T, R] = {
    new SelectWhere[T, R](table, qb.and(condition(table).clause), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

  def setFetchSize(n: Int) = {
    qb.setFetchSize(n)
    this
  }

  def and = where _


}
