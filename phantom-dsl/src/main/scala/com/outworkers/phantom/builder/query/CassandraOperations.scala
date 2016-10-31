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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core._
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.outworkers.phantom.Manager
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}

import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture, Promise => ScalaPromise}

private[phantom] trait CassandraOperations extends SessionAugmenterImplicits {

  protected[this] def scalaQueryStringExecuteToFuture(st: Statement)(
    implicit session: Session,
    keyspace: KeySpace,
    executor: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringToPromise(st).future
  }

  protected[this] def preparedStatementToPromise(st: String)(
    implicit session: Session,
    keyspace: KeySpace,
    executor: ExecutionContextExecutor
  ): ScalaPromise[PreparedStatement] = {
    Manager.logger.debug(s"Executing query: ${st.toString}")

    val promise = ScalaPromise[PreparedStatement]()

    val future = session.prepareAsync(st)

    val callback = new FutureCallback[PreparedStatement] {
      def onSuccess(result: PreparedStatement): Unit = {
        promise success result
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise failure err
      }
    }

    Futures.addCallback(future, callback, executor)
    promise
  }

  protected[this] def scalaQueryStringToPromise(st: Statement)(
    implicit session: Session,
    keyspace: KeySpace,
    executor: ExecutionContextExecutor
  ): ScalaPromise[ResultSet] = {
    Manager.logger.debug(s"Executing query: ${st.toString}")

    val promise = ScalaPromise[ResultSet]()

    val future = session.executeAsync(st)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise success result
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise failure err
      }
    }

    Futures.addCallback(future, callback, executor)
    promise
  }
}
