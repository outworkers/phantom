/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.database

import com.datastax.driver.core.{ResultSet, Session}
import com.outworkers.phantom.{CassandraTable, Manager}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.{CQLQuery, CreateQuery, ExecutableStatementList}
import com.outworkers.phantom.connectors.{KeySpace, CassandraConnection}
import com.outworkers.phantom.macros.DatabaseHelper

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future, blocking}

private object Lock

abstract class Database[
  DB <: Database[DB]
](val connector: CassandraConnection)(implicit helper: DatabaseHelper[DB]) {

  trait Connector extends connector.Connector

  implicit val space: KeySpace = KeySpace(connector.name)

  implicit lazy val session: Session = connector.session

  val tables: Set[CassandraTable[_, _]] = helper.tables(this.asInstanceOf[DB])

  def shutdown(): Unit = {
    blocking {
      Manager.shutdown()
      session.getCluster.close()
      session.close()
    }
  }

  private[this] val defaultTimeout = 10.seconds

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to create the entire database schema in a single call.
   *
   * Every future in the statement list will contain the CQL schema generation query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  private[phantom] def autocreate(): ExecutableCreateStatementsList = helper.createQueries(this.asInstanceOf[DB])

  /**
    * A blocking method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def create(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(createAsync(), timeout)
  }

  /**
    * An asynchronous method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    autocreate().future()
  }

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to drop the entire database schema in a single call.
   *
   * Every future in the statement list will contain the ALTER DROP drop query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  private[phantom] def autodrop(): ExecutableStatementList = {
    new ExecutableStatementList(tables.toSeq.map {
      table => table.alter().drop().qb
    })
  }

  /**
    * An async method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def dropAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    autodrop().future()
  }

  /**
    * A blocking method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def drop(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(dropAsync(), timeout)
  }

  /**
   * Returns a list of executable statements that will be parallelized with futures
   * to truncate the entire database schema in a single call.
   *
   * Every future in the statement list will contain the CQL truncation query
   * for a single table. Processing order is not guaranteed however the tables
   * are generally processed in the order they are written in, even though
   * ordering is not a guarantee required at this level of an application.
   *
   * @return An executable statement list that can be used with Scala or Twitter futures to simultaneously
   *         execute an entire sequence of queries.
   */
  private[phantom] def autotruncate(): ExecutableStatementList = {
    new ExecutableStatementList(tables.toSeq.map(_.truncate().qb))
  }

  /**
    * A blocking method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncate(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(truncateAsync(), timeout)
  }

  /**
    * An async method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
 *
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncateAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    autotruncate().future()
  }
}

sealed class ExecutableCreateStatementsList(val queries: KeySpace => Seq[CreateQuery[_, _, _]]) {
  def future()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): Future[Seq[ResultSet]] = {
    Future.sequence(queries(keySpace).map(_.future()))
  }
}
