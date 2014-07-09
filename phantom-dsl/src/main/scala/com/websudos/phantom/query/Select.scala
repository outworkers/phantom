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

import scala.concurrent.{ ExecutionContext, Future }

import com.datastax.driver.core.{ Row, Session }
import com.datastax.driver.core.querybuilder.Select

import com.websudos.phantom.CassandraTable
import com.twitter.util.{ Future => TwitterFuture }

import play.api.libs.iteratee.{ Iteratee => PlayIteratee }


class SelectQuery[T <: CassandraTable[T, _], R](table: T, protected[phantom] val qb: Select, rowFunc: Row => R) extends CQLQuery[SelectQuery[T, R]] with ExecutableQuery[T, R] {

  override def fromRow(r: Row): R = rowFunc(r)

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), rowFunc)
    query.fetchEnumerator run PlayIteratee.head
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This will always use a LIMIT 1 in the Cassandra query.
   * @param session The Cassandra session in use.
   * @return
   */
  def get()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), rowFunc)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
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

  def limit(l: Int): SelectQuery[T, R] = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }

}

class SelectCountQuery[T <: CassandraTable[T, _], R](table: T, qb: Select, rowFunc: Row => R) extends SelectQuery[T, R](table, qb, rowFunc) {

  /**
   * Where clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @tparam RR The type of the underlying abstract column.
   * @return A SelectCountWhere.
   */
  override def where[RR](condition: T => QueryCondition): SelectCountWhere[T, R] = {
    new SelectCountWhere[T, R](table, qb.where(condition(table).clause), fromRow)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This method will not enforce a LIMIT 1 on the "one" query method.
   * It is used to extract the record count obtained from a SELECT COUNT(*).
   * If a count query is executed with a LIMIT, Cassandra will limit the records before counting.
   *
   * If that count has a limit, the return is always less or equal to the limit, which is wrong.
   *
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return A Future wrapping an Optional result.
   */
  override def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb, fromRow)
    query.fetchEnumerator run PlayIteratee.head
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This method will not enforce a LIMIT 1 on the "one" query method.
   * It is used to extract the record count obtained from a SELECT COUNT(*).
   * If a count query is executed with a LIMIT, Cassandra will limit the records before counting.
   *
   * In this case, the count is always less or equal to the limit.
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return A Future wrapping an Optional result.
   */
  override def get()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb, fromRow)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
  }
}


class SelectWhere[T <: CassandraTable[T, _], R](val table: T, val qb: Select.Where, rowFunc: Row => R) extends CQLQuery[SelectWhere[T, R]] with ExecutableQuery[T, R] {

  override def fromRow(r: Row): R = rowFunc(r)

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
    query.fetchEnumerator run PlayIteratee.head
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @return
   */
  def get()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectQuery[T, R](table, qb.limit(1), fromRow)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
  }

  def and[RR](condition: T => QueryCondition): SelectWhere[T, R] = {
    new SelectWhere[T, R](table, qb.and(condition(table).clause), fromRow)
  }

  def limit(l: Int): SelectQuery[T, R] = {
    new SelectQuery(table, qb.limit(l), fromRow)
  }
}

class SelectCountWhere[T <: CassandraTable[T, _], R](table: T, qb: Select.Where, rowFunc: Row => R) extends SelectWhere[T, R](table, qb, rowFunc) {

  /**
   * Returns the first row from the select ignoring everything else
   * This method will not enforce a LIMIT 1 on the "one" query method.
   * It is used to extract the record count obtained from a SELECT COUNT(*).
   * If a count query is executed with a LIMIT, Cassandra will limit the records before counting.
   *
   * In this case, the count is always less or equal to the limit.
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return A Future wrapping an Optional result.
   */
  override def one()(implicit session: Session, ctx: scala.concurrent.ExecutionContext): Future[Option[R]] = {
    val query = new SelectCountWhere[T, R](table, qb, fromRow)
    query.fetchEnumerator run PlayIteratee.head
  }

  override def limit(l: Int): SelectCountQuery[T, R] = {
    new SelectCountQuery(table, qb.limit(l), fromRow)
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This method will not enforce a LIMIT 1 on the "one" query method.
   * It is used to extract the record count obtained from a SELECT COUNT(*).
   * If a count query is executed with a LIMIT, Cassandra will limit the records before counting.
   *
   * In this case, the count is always less or equal to the limit.
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return A Future wrapping an Optional result.
   */
  override def get()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Option[R]] = {
    val query = new SelectCountWhere[T, R](table, qb, fromRow)
    query.enumerate() flatMap {
      res => {
        scalaFutureToTwitter(res run PlayIteratee.head)
      }
    }
  }

  /**
   * And clauses require overriding for count queries for the same purpose.
   * Without this override, the CQL query executed to fetch the count would still have a "LIMIT 1".
   * @param condition The Query condition to execute, based on index operators.
   * @tparam RR The type of the underlying abstract column.
   * @return A SelectCountWhere.
   */
  override def and[RR](condition: T => QueryCondition): SelectCountWhere[T, R] = {
    new SelectCountWhere[T, R](table, qb.and(condition(table).clause), fromRow)
  }
}

