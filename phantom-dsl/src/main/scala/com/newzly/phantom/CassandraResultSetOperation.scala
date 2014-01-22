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
package com.newzly.phantom

import java.util.concurrent.{Executors, LinkedBlockingQueue, TimeUnit, ThreadPoolExecutor}
import com.datastax.driver.core.{ ResultSet, Session, Statement }
import com.google.common.util.concurrent.{
  Futures,
  FutureCallback,
  ListeningExecutorService,
  MoreExecutors,
  ThreadFactoryBuilder
}
import com.twitter.util.{ Future, NonFatal, Promise }
import scala.concurrent.ExecutionContext.Implicits.global

object Manager {
  private[this] lazy val processors = Runtime.getRuntime.availableProcessors()

  private[this] def makeExecutor(threads: Int, name: String) : ListeningExecutorService = {
    MoreExecutors.listeningDecorator(Executors.newCachedThreadPool())
  }

  lazy val executor = makeExecutor(
    processors,
    "Cassandra Java Driver worker-%d"
  )
}

trait CassandraResultSetOperations {

  /**
   * Converts a statement to a result Future.
   * @param s The statement to execute.
   * @param session The Cassandra cluster connection session to use.
   * @return
   */
  def statementExecuteToFuture(s: Statement)(implicit session: Session): Future[ResultSet] = {
    val promise = Promise[ResultSet]()
    val future = session.executeAsync(s)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise become Future.value(result)
      }

      def onFailure(err: Throwable): Unit = {
        promise raise err
      }
    }
    Futures.addCallback(future, callback, Manager.executor)

    promise
  }

  def queryStringExecuteToFuture(s: String)(implicit session: Session): Future[ResultSet] = {
    val promise = Promise[ResultSet]()

    val future = session.executeAsync(s)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise become Future.value(result)
      }

      def onFailure(err: Throwable): Unit = {
        promise raise err
      }
    }
    Futures.addCallback(future, callback, Manager.executor)
    promise
  }
}

object CassandraResultSetOperations extends CassandraResultSetOperations