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
package com.outworkers.phantom.streams

import akka.Done
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}
import com.outworkers.phantom.builder.query.CassandraOperations

import scala.concurrent.{ExecutionContext, Future}

object CassandraSink extends CassandraOperations {
  def apply[T](
    parallelism: Int,
    statement: PreparedStatement,
    statementBinder: (T, PreparedStatement) => BoundStatement
  )(implicit session: Session, ex: ExecutionContext): Sink[T, Future[Done]] =
    Flow[T]
      .mapAsyncUnordered(parallelism)(t ⇒ scalaQueryStringExecuteToFuture(statementBinder(t, statement)))
      .toMat(Sink.ignore)(Keep.right)
}
