package com.newzly.phantom.finagle

import scala.collection.JavaConverters._
import com.datastax.driver.core.{ BatchStatement => DatastaxBatchStatement, ResultSet, Session, Statement}
import com.google.common.util.concurrent.{Futures, FutureCallback}
import com.newzly.phantom.{CassandraTable, CassandraResultSetOperations, Manager}
import com.newzly.phantom.batch.BatchStatement
import com.newzly.phantom.query._
import com.twitter.util.{Duration, Future, Promise}
import com.newzly.phantom.iteratee.{Iteratee, Enumerator}
import play.api.libs.iteratee.{Enumerator => PlayEnumerator, Iteratee => PlayIteratee, Enumeratee}
import scala.concurrent.{Future => ScalaFuture, ExecutionContext}

object Implicits {
  implicit def transformToTwitterFuture[R](sf: ScalaFuture[R])(implicit executorContext: ExecutionContext = Manager.scalaExecutor): Future[R] = {
      val p = new Promise[R]

      sf.onSuccess {
        case r => p become Future.value(r)
      }

      sf.onFailure {
        case t => p raise t
      }

      p
  }
}
