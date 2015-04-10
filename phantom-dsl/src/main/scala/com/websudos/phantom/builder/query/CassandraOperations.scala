package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ResultSet, Session}
import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.twitter.util.{Future => TwitterFuture, Promise => TwitterPromise, Return, Throw}
import com.websudos.phantom.Manager
import com.websudos.phantom.connectors.KeySpace

import scala.concurrent.{ExecutionContext, Future => ScalaFuture, Promise => ScalaPromise}
import scala.util.{Failure, Success}

private[phantom] trait CassandraOperations {

  protected[this] def scalaQueryStringExecuteToFuture(query: String)(implicit session: Session, keyspace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringToPromise(query).future
  }

  protected[this] def scalaQueryStringToPromise(query: String)(implicit session: Session, keyspace: KeySpace): ScalaPromise[ResultSet] = {
    Manager.logger.debug(s"Executing Cassandra query: $query")

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
    promise
  }


  protected[this] def twitterQueryStringExecuteToFuture(query: String)(implicit session: Session, keyspace: KeySpace): TwitterFuture[ResultSet] = {
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
