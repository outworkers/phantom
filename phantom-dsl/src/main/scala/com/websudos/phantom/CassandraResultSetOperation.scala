/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom

import java.util.concurrent.Executors
import scala.concurrent.{ ExecutionContext, Future => ScalaFuture, Promise => ScalaPromise }
import scala.util.{ Failure, Success }
import org.slf4j.LoggerFactory
import com.datastax.driver.core.{ ResultSet, Session, Statement }
import com.google.common.util.concurrent.{
  Futures,
  FutureCallback,
  MoreExecutors
}
import com.twitter.util.{ Future => TwitterFuture, Promise => TwitterPromise, Return, Throw }

private[phantom] object Manager {

  lazy val cores = Runtime.getRuntime.availableProcessors()

  lazy val taskExecutor = Executors.newCachedThreadPool()

  implicit lazy val scalaExecutor: ExecutionContext = ExecutionContext.fromExecutor(taskExecutor)

  lazy val executor = MoreExecutors.listeningDecorator(taskExecutor)

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom")
}

private[phantom] trait CassandraResultSetOperations {

  protected[this] def scalaStatementToFuture(s: Statement)(implicit session: Session): ScalaFuture[ResultSet] = {
    val promise = ScalaPromise[ResultSet]()

    val future = session.executeAsync(s)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise success result
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise failure err
      }
    }
    Futures.addCallback(future, callback, Manager.executor)
    promise.future

  }

  protected[this] def twitterStatementToFuture(s: Statement)(implicit session: Session): TwitterFuture[ResultSet] = {
    val promise = TwitterPromise[ResultSet]()
    val future = session.executeAsync(s)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise update Return(result)
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise update Throw(err)
      }
    }
    Futures.addCallback(future, callback, Manager.executor)
    promise

  }

  protected[this] def scalaQueryStringExecuteToFuture(query: String)(implicit session: Session): ScalaFuture[ResultSet] = {
    Manager.logger.debug("Executing Cassandra query:")
    Manager.logger.debug(query)
    val promise = ScalaPromise[ResultSet]()

    val future = session.executeAsync(query)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise success result
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise failure err
      }
    }
    Futures.addCallback(future, callback, Manager.executor)
    promise.future
  }

  protected[this] def twitterQueryStringExecuteToFuture(query: String)(implicit session: Session): TwitterFuture[ResultSet] = {
    val promise = TwitterPromise[ResultSet]()
    val future = session.executeAsync(query)

    val callback = new FutureCallback[ResultSet] {
      def onSuccess(result: ResultSet): Unit = {
        promise update Return(result)
      }

      def onFailure(err: Throwable): Unit = {
        Manager.logger.error(err.getMessage)
        promise update Throw(err)
      }
    }
    Futures.addCallback(future, callback, Manager.executor)
    promise
  }

  protected[this] def scalaFutureToTwitter[R](future: ScalaFuture[R])(implicit ctx: ExecutionContext): TwitterFuture[R] = {
    val promise = TwitterPromise[R]()

    future onComplete {
      case Success(res) => promise update Return(res)
      case Failure(err) => {
        Manager.logger.error(err.getMessage)
        promise update Throw(err)
      }
    }
    promise
  }
}
