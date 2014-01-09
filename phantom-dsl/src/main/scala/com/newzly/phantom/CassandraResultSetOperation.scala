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
package com
package newzly
package phantom

import java.util.concurrent.{ LinkedBlockingQueue, TimeUnit, ThreadPoolExecutor }
import com.datastax.driver.core.{ ResultSet, Session, Statement }
import com.google.common.util.concurrent.{
  ListeningExecutorService,
  MoreExecutors,
  ThreadFactoryBuilder
}
import com.twitter.util.{ Future, NonFatal, Promise }

object Manager {
  private[this] final val DEFAULT_THREAD_KEEP_ALIVE: Int = 30

  private[this] def makeExecutor(threads: Int, name: String) : ListeningExecutorService = {
    val executor: ThreadPoolExecutor = new ThreadPoolExecutor(
      threads,
      threads,
      DEFAULT_THREAD_KEEP_ALIVE,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable],
      new ThreadFactoryBuilder().setNameFormat(name).build()
    )
    executor.allowCoreThreadTimeOut(true)
    MoreExecutors.listeningDecorator(executor)
  }

  lazy val executor = makeExecutor(
    Runtime.getRuntime.availableProcessors,
    "Cassandra Java Driver worker-%d"
  )
}

trait CassandraResultSetOperations {
  def statementExecuteToFuture(s: Statement)(implicit session: Session): Future[ResultSet] = {
    val promise = Promise[ResultSet]()

    val future = session.executeAsync(s)
    future.addListener(new Runnable {
      override def run(): Unit = {
        try {
          promise become Future.value(future.get)
        } catch {
          case NonFatal(e) => promise raise e
        }
      }
    }, Manager.executor)
    promise
  }

  def queryStringExecuteToFuture(s: String)(implicit session: Session): Future[ResultSet] = {
    val promise = Promise[ResultSet]()

    val future = session.executeAsync(s)
    future.addListener(new Runnable {
      def run(): Unit = try {
        promise become Future.value(future.get)
      } catch {
        case NonFatal(e) => promise.raise(e)
      }
    }, Manager.executor)

    promise
  }
}

object CassandraResultSetOperations extends CassandraResultSetOperations