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

import scala.concurrent.{ ExecutionContext, Future => ScalaFuture }
import com.datastax.driver.core.{ Row, ResultSet, Session, Statement }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable }
import play.api.libs.iteratee.{Enumerator => PlayEnumerator, Iteratee => PlayIteratee, Enumeratee}
import com.newzly.phantom.iteratee.{ Enumerator, Iteratee }
import com.twitter.util.{ Future => TwitterFuture }

trait ExecutableStatement extends CassandraResultSetOperations {

  def qb: Statement

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    scalaStatementToFuture(qb)
  }

  def execute()(implicit  session: Session): TwitterFuture[ResultSet] = {
    twitterStatementToFuture(qb)
  }
}

/**
 *
 * @tparam T The class owning the table.
 * @tparam R The record type to store.
 */
trait ExecutableQuery[T <: CassandraTable[T, _], R] extends CassandraResultSetOperations {

  def qb: Statement
  def table: CassandraTable[T, _]
  def fromRow(r: Row): R

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    table.logger.info(qb.toString)
    scalaStatementToFuture(qb)
  }

  def execute()(implicit  session: Session): TwitterFuture[ResultSet] = {
    table.logger.info(qb.toString)
    twitterStatementToFuture(qb)
  }

  /**
   * Produces an Enumerator for [R]ows
   * This enumerator can be consumed afterwards with an Iteratee
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def fetchEnumerator()(implicit session: Session, ctx: ExecutionContext): ScalaFuture[PlayEnumerator[R]] = {
    future() map {
      resultSet => {
        Enumerator.enumerator(resultSet) through Enumeratee.map(r => this.fromRow(r))
      }
    }
  }

  def enumerate()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[PlayEnumerator[R]] = {
    execute() map {
      resultSet => {
        Enumerator.enumerator(resultSet) through Enumeratee.map(r => this.fromRow(r))
      }
    }
  }

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: ExecutionContext): ScalaFuture[Option[R]]

  /**
   * Get the result of an operation as a Twitter Future.
   * @param session The Datastax Cassandra session.
   * @return A Twitter future wrapping the result.
   */
  def get()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Option[R]]

  /**
   * Returns a parsed sequence of [R]ows
   * This is not suitable for big results set
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def fetch()(implicit session: Session, ctx: ExecutionContext): ScalaFuture[Seq[R]] = {
    table.logger.info(qb.toString)
    fetchEnumerator flatMap(_ run Iteratee.collect())
  }

  /**
   * Returns a parsed sequence of [R]ows
   * This is not suitable for big results set
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def collect()(implicit session: Session, ctx: ExecutionContext): TwitterFuture[Seq[R]] = {
    table.logger.info(qb.toString)
    enumerate flatMap {
      res => {
        scalaFutureToTwitter(res run Iteratee.collect())
      }
    }
  }
}
