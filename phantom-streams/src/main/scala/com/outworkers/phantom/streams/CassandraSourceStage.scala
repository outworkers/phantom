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

import akka.stream._
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, OutHandler}
import com.datastax.driver.core.{ResultSet, Row, Session, Statement}
import com.outworkers.phantom.builder.query.CassandraOperations

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final class CassandraSourceStage(
  futStmt: Future[Statement]
)(implicit session: Session) extends GraphStage[SourceShape[Row]] with CassandraOperations {
  val out: Outlet[Row] = Outlet("CassandraSource.out")
  override val shape: SourceShape[Row] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      var maybeRs = Option.empty[ResultSet]
      var futFetchedCallback: AsyncCallback[Try[ResultSet]] = _

      override def preStart(): Unit = {
        implicit val ec = materializer.executionContext

        futFetchedCallback = getAsyncCallback[Try[ResultSet]](tryPushAfterFetch)

        val futRs = futStmt.flatMap(scalaQueryStringExecuteToFuture)
        futRs.onComplete(futFetchedCallback.invoke)
      }

      setHandler(
        out,
        new OutHandler {
          override def onPull(): Unit = {
            implicit val ec = materializer.executionContext

            maybeRs match {
              case Some(rs) if rs.getAvailableWithoutFetching > 0 => push(out, rs.one())
              case Some(rs) if rs.isExhausted => completeStage()
              case Some(rs) =>
                // fetch next page
                val futRs = guavaFutureAsScala(rs.fetchMoreResults())
                futRs.onComplete(futFetchedCallback.invoke)
              case None => () // doing nothing, waiting for futRs in preStart() to be completed
            }
          }
        }
      )

      private def tryPushAfterFetch(rsOrFailure: Try[ResultSet]): Unit = rsOrFailure match {
        case Success(rs) =>
          maybeRs = Some(rs)
          if (rs.getAvailableWithoutFetching > 0) {
            if (isAvailable(out)) {
              push(out, rs.one())
            }
          } else {
            completeStage()
          }

        case Failure(failure) => failStage(failure)
      }
    }
}

