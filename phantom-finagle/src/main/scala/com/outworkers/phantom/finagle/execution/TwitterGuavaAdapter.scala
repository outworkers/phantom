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
package com.outworkers.phantom.finagle.execution

import com.datastax.driver.core.{Session, Statement, ResultSet => DatastaxResultSet}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import com.outworkers.phantom.builder.batch.BatchWithQuery
import com.outworkers.phantom.builder.query.execution.GuavaAdapter
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import com.outworkers.phantom.{Manager, ResultSet}
import com.twitter.util.{Future, Promise}

import scala.concurrent.ExecutionContextExecutor

object TwitterGuavaAdapter extends GuavaAdapter[Future] with SessionAugmenterImplicits {

  protected[this] def statementToFuture(st: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Future[ResultSet] = {
    statementToPromise(st)
  }

  protected[this] def batchToPromise(batch: BatchWithQuery)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Promise[ResultSet] = {
    Manager.logger.debug(s"Executing query {}", batch.debugString)
    statementToPromise(batch.statement)
  }

  def guavaToScala[T](source: ListenableFuture[T])(
    implicit executor: ExecutionContextExecutor
  ): Promise[T] = {
    val promise = Promise[T]()

    val callback = new FutureCallback[T] {
      def onSuccess(result: T): Unit = {
        promise setValue result
      }

      def onFailure(err: Throwable): Unit = {
        promise raise err
      }
    }

    Futures.addCallback(source, callback, executor)
    promise
  }

  protected[this] def statementToPromise(st: Statement)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): Promise[ResultSet] = {
    Manager.logger.debug(s"Executing query {}", st)

    val promise = Promise[ResultSet]()

    val future = session.executeAsync(st)

    val callback = new FutureCallback[DatastaxResultSet] {
      def onSuccess(result: DatastaxResultSet): Unit = {
        promise setValue ResultSet(result, session.protocolVersion)
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(s"Failed to execute query $st", err)
        promise raise err
      }
    }

    Futures.addCallback(future, callback, executor)
    promise
  }

  override def fromGuava(in: Statement)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): Future[ResultSet] = statementToFuture(in)

  override def fromGuava[T](source: ListenableFuture[T])(
    implicit executor: ExecutionContextExecutor
  ): Future[T] = {

    val promise = Promise[T]()

    val callback = new FutureCallback[T] {
      def onSuccess(result: T): Unit = {
        promise setValue result
      }

      def onFailure(err: Throwable): Unit = {
        promise raise err
      }
    }

    Futures.addCallback(source, callback, executor)
    promise
  }
}