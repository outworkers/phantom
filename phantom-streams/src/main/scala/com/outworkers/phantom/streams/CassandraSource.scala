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

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core._
import scala.concurrent.Future

object CassandraSource {

  /**
    * Scala API: creates a [[CassandraSourceStage]] from a given statement.
    */
  def apply(stmt: Statement)(implicit session: Session): Source[Row, NotUsed] =
  Source.fromGraph(new CassandraSourceStage(Future.successful(stmt))

  /**
    * Scala API: creates a [[CassandraSourceStage]] from the result of a given Future.
    */
  def fromFuture(futStmt: Future[Statement])(implicit session: Session): Source[Row, NotUsed] =
  Source.fromGraph(new CassandraSourceStage(futStmt))

}
