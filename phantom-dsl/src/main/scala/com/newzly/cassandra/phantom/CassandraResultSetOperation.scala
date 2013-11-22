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
package cassandra
package phantom

import java.util.concurrent.TimeUnit
import scala.util.{ Try, Success }

import com.datastax.driver.core.{ ResultSetFuture, ResultSet }

import com.twitter.finagle.AbstractCodec
import com.twitter.finagle.Service
import com.twitter.util.{ Await, Future }
import com.twitter.util.Awaitable.CanAwait
import com.twitter.util.Duration

trait CassandraResultSetOperations {

  protected[cassandra] implicit class RichResultSetFuture(resultSetFuture: ResultSetFuture) extends Future[ResultSet] {
    @throws(classOf[InterruptedException])
    @throws(classOf[com.twitter.util.TimeoutException])
    def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
      resultSetFuture.get(atMost.inMillis, TimeUnit.MILLISECONDS)
      this
    }

    @throws(classOf[Exception])
    def result(atMost: Duration)(implicit permit: CanAwait): ResultSet = {
      resultSetFuture.get(atMost.inMillis, TimeUnit.MILLISECONDS)
    }

    def onComplete[U](func: (Try[ResultSet]) => U)(implicit permit: CanAwait): Unit = {
      func(Success(resultSetFuture.getUninterruptibly))
    }

    def isCompleted: Boolean = resultSetFuture.isDone

    def raise(interrup: Throwable): Unit = {

    }

    def value: Option[Try[ResultSet]] = if (resultSetFuture.isDone) Some(Try(resultSetFuture.get())) else None
  }
}

object CassandraResultSetOperations extends CassandraResultSetOperations