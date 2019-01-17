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
package com.outworkers.phantom.monix

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.phantom.{PhantomSuite, connectors}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import scala.util.{Failure, Success}

object TestConnector {

  val default: CassandraConnection = connectors.ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
    )
    ).noHeartbeat().keySpace(
    KeySpace("phantom").ifNotExists().`with`(
      replication eqs SimpleStrategy.replication_factor(1)
    )
  )
}


object MonixDatabase extends TestDatabase(TestConnector.default)

trait MonixSuite extends PhantomSuite {
  override def db: TestDatabase = MonixDatabase

  implicit def taskFutureConcept[T](f: Task[T]): FutureConcept[T] = new FutureConcept[T] {

    private[this] val source = f.memoize

    override def eitherValue: Option[Either[Throwable, T]] = {
      source.runAsync.value match {
        case Some(Success(ret)) => Some(Right(ret))
        case Some(Failure(err)) => Some(Left(err))
        case None => None
      }
    }

    override def isExpired: Boolean = false

    override def isCanceled: Boolean = false
  }
}