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

import scala.concurrent.{ ExecutionContext, Future }
import com.datastax.driver.core.{ Row, Session }
import com.datastax.driver.core.querybuilder.{ Select, Ordering }
import com.newzly.phantom.CassandraTable
import com.twitter.util.{ Future => TwitterFuture }
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

  def orderBy[RR](conditions: (T => QueryOrdering)*): SelectQuery[T, R] = {
    val applied = conditions map {
      fn => fn(table).ordering
    }
    new SelectQuery[T, R](table, qb.orderBy(applied: _*), fromRow)
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
    val query = new SelectQuery[T, R](table, qb.limit(1), fromRow)
    table.logger.info(query.qb.toString)
    query.fetchEnumerator flatMap(_ run PlayIteratee.head)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This will always use a LIMIT 1 in the Cassandra query.
   * @param session The Cassandra session in use.
   * @return
   */
  def get()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), fromRow)
    table.logger.info(query.qb.toString)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
  }
}

class SelectWhere[T <: CassandraTable[T, _], R](val table: T, val qb: Select.Where, rowFunc: Row => R) extends ExecutableQuery[T, R] {

  override def fromRow(r: Row) = rowFunc(r)

  def orderBy[RR](conditions: (T => QueryOrdering)*): SelectQuery[T, R] = {
    val applied = conditions map {
      fn => fn(table).ordering
    }
    new SelectQuery[T, R](table, qb.orderBy(applied: _*), fromRow)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), fromRow)
    table.logger.info(query.qb.toString)
    query.fetchEnumerator flatMap(_ run PlayIteratee.head)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @return
   */
  def get()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), fromRow)
    table.logger.info(query.qb.toString)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
  }

  def and[RR](condition: T => QueryCondition): SelectWhere[T, R] = {
    new SelectWhere[T, R](table, qb.and(condition(table).clause), fromRow)
  }

  def limit(l: Int) = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

  def setFetchSize(n: Int) = {
    qb.setFetchSize(n)
    this
  }
}
