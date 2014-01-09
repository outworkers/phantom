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

import java.util.concurrent.{Executors, Executor, TimeUnit}
//import scala.concurrent.{ CanAwait }
import scala.concurrent.duration._
import scala.util.{ Try, Success}
import com.twitter.util.Awaitable.CanAwait
import com.twitter.util.{TimeoutException, Promise, Duration, Future}
import com.datastax.driver.core.{Statement, ResultSetFuture, ResultSet, Session}
import com.twitter.util

trait CassandraResultSetOperations {
  def statementExecuteToFuture(s: Statement)(implicit session: Session): Future[ResultSet] = {
    val p: Future[ResultSetFuture] = Future(session.executeAsync(s))
    p map { r => r.getUninterruptibly}
  }

  def queryStringExecuteToFuture(s: String)(implicit session: Session): Future[ResultSet] = {
    val p: Future[ResultSetFuture] = Future(session.executeAsync(s))
    p map { r => r.getUninterruptibly}
  }
}

object CassandraResultSetOperations extends CassandraResultSetOperations