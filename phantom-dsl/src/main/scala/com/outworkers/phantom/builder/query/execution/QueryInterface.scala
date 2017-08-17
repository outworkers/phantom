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

import com.datastax.driver.core.{Session, Statement}
import com.outworkers.phantom.ResultSet

import scala.concurrent.ExecutionContextExecutor

trait MultiQueryInterface[M[X] <: TraversableOnce[X], F[_]] {

  def future()(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[M[ResultSet]]

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
  ): F[M[ResultSet]]
}


abstract class QueryInterface[F[_]]()(implicit adapter: GuavaAdapter[F]) {

  def executableQuery: ExecutableCqlQuery

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
  ): F[ResultSet] = {
    adapter.fromGuava(executableQuery)
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
  ): F[ResultSet] = adapter.fromGuava(modifyStatement(executableQuery.statement()))
}

