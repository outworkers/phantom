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
package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.{Session, SimpleStatement, Statement}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.concurrent.ExecutionContextExecutor

abstract class QueryInterface[M[_]]()(implicit adapter: GuavaAdapter[M]) {

  def options: QueryOptions

  def qb: CQLQuery

  def queryString: String = qb.terminate.queryString

  def statement()(implicit session: Session): Statement = {
    options(new SimpleStatement(qb.terminate.queryString))
  }

  /**
    * Default asynchronous query execution method. This will convert the underlying
    * call to Cassandra done with Google Guava ListenableFuture to a consumable
    * Scala Future that will be completed once the operation is completed on the
    * database end.
    *
    * The execution context of the transformation is provided by phantom via
    * based on the execution engine used.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): M[ResultSet] = {
    adapter.fromGuava(statement)
  }

  /**
    * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
    * Scala Future that will be completed once the operation is completed on the
    * database end.
    *
    * The execution context of the transformation is provided by phantom via
    * based on the execution engine used.
    *
    * @param modifyStatement The function allowing to modify underlying [[Statement]]
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param executor The implicit Scala executor.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future(modifyStatement: Statement => Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): M[ResultSet] = adapter.fromGuava(modifyStatement(statement))
}

