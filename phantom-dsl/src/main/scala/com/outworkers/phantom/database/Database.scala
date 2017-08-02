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

import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.connectors.{CassandraConnection, KeySpace}
import com.outworkers.phantom.macros.DatabaseHelper

import scala.concurrent.blocking

abstract class Database[
  DB <: Database[DB]
](val connector: CassandraConnection)(
  implicit helper: DatabaseHelper[DB]
) {

  trait Connector extends connector.Connector

  implicit val space: KeySpace = KeySpace(connector.name)

  implicit lazy val session: Session = connector.session

  val tables: Seq[CassandraTable[_, _]] = helper.tables(this.asInstanceOf[DB])

  def shutdown(): Unit = {
    blocking {
      session.getCluster.close()
      session.close()
    }
  }

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
  private[phantom] def autocreate(): QueryCollection[Seq] = helper.createQueries(this.asInstanceOf[DB])

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
  private[phantom] def autodrop(): QueryCollection[Seq] = {
    new QueryCollection(tables.map { table =>
      ExecutableCqlQuery(table.alter().drop().qb)
    })
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
  private[phantom] def autotruncate(): QueryCollection[Seq] = {
    new QueryCollection(tables.map(table => ExecutableCqlQuery(table.truncate().qb)))
  }
}