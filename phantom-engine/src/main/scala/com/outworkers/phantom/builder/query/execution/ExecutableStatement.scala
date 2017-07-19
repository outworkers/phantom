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

import com.datastax.driver.core.{Session, Statement, ResultSet => DatastaxResultSet}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.batch.BatchWithQuery
import com.outworkers.phantom.connectors.SessionAugmenterImplicits

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

trait ExecutableStatement extends QueryInterface[Future] with SessionAugmenterImplicits {

  protected[this] def statementToFuture(st: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = {
    fromGuava(st).future
  }

  protected[this] def batchToPromise(batch: BatchWithQuery)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Promise[ResultSet] = {
    //Manager.logger.debug(s"Executing query: ${batch.debugString}")
    fromGuava(batch.statement)
  }

  override def fromGuava(st: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Promise[ResultSet] = {
    //Manager.logger.debug(s"Executing query: $st")

    val promise = Promise[ResultSet]()

    val future = session.executeAsync(st)

    val callback = new FutureCallback[DatastaxResultSet] {
      def onSuccess(result: DatastaxResultSet): Unit = {
        promise success ResultSet(result, session.protocolVersion)
      }

      def onFailure(err: Throwable): Unit = {
        //Manager.logger.error(s"Failed to execute query $st", err)
        promise failure err
      }
    }

    Futures.addCallback(future, callback, executor)
    promise
  }
}
