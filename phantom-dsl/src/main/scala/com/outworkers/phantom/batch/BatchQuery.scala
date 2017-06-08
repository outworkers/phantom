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
package com.outworkers.phantom.batch

import com.datastax.driver.core.{BatchStatement, ConsistencyLevel, Session, Statement}
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

case class BatchWithQuery(
  statement: Statement,
  queries: List[String],
  batchType: BatchType
) {
  def debugString: String = s"BEGIN BATCH $batchType (${queries.mkString("\n")}) APPLY BATCH;"
}

class BatchType(val batch: String)

object BatchType {
  case object Logged extends BatchType(CQLSyntax.Batch.Logged)
  case object Unlogged extends BatchType(CQLSyntax.Batch.Unlogged)
  case object Counter extends BatchType(CQLSyntax.Batch.Counter)
}

sealed class BatchQuery[Status <: ConsistencyBound](
  val iterator: Iterator[Batchable with ExecutableStatement],
  batchType: BatchType,
  usingPart: UsingPart = UsingPart.empty,
  added: Boolean = false,
  override val options: QueryOptions
) extends ExecutableStatement {

  override def future()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    batchToPromise(makeBatch()).future
  }

  def initBatch(): BatchStatement = batchType match {
    case BatchType.Logged => new BatchStatement()
    case BatchType.Unlogged => new BatchStatement(BatchStatement.Type.UNLOGGED)
    case BatchType.Counter => new BatchStatement(BatchStatement.Type.COUNTER)
  }

  def makeBatch()(implicit session: Session): BatchWithQuery = {
    val batch = initBatch()

    val builder = List.newBuilder[String]

    for (st <- iterator) {
      builder += st.queryString
      batch.add(st.statement())
    }

    val statement = options.consistencyLevel match {
      case Some(level) => batch.setConsistencyLevel(level)
      case None => batch
    }

    BatchWithQuery(statement, builder.result(), batchType)
  }


  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  final def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified,
    session: Session
  ): BatchQuery[Specified] = {
    if (session.protocolConsistency) {
      new BatchQuery[Specified](
        iterator,
        batchType,
        usingPart,
        added,
        options.consistencyLevel_=(level)
      )
    } else {
      new BatchQuery[Specified](
        iterator,
        batchType,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        added,
        options
      )
    }
  }

  def add(query: Batchable with ExecutableStatement)(implicit session: Session): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ Iterator(query),
      batchType,
      usingPart,
      added,
      options
    )
  }

  def add(queries: Batchable with ExecutableStatement*)(implicit session: Session): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries.iterator,
      batchType,
      usingPart,
      added,
      options
    )
  }

  def add(queries: Iterator[Batchable with ExecutableStatement])(implicit session: Session): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries,
      batchType,
      usingPart,
      added,
      options
    )
  }

  /**
    * Adds the statement of another query to the statements of this batch query.
    * @param batch A batch query.
    * @return A batch query with the same consistencyLevel as the original query.
    */
  def add(batch: BatchQuery[_]): BatchQuery[Status] = {
    new BatchQuery[Status](
      this.iterator ++ batch.iterator,
      this.batchType,
      this.usingPart,
      this.added,
      this.options
    )
  }

  def timestamp(stamp: Long): BatchQuery[Status] = {
    new BatchQuery(
      iterator,
      batchType,
      usingPart append QueryBuilder.timestamp(stamp),
      added,
      options
    )
  }

  override def qb: CQLQuery = CQLQuery.empty
}

private[phantom] trait Batcher {

  def apply(batchType: String = CQLSyntax.Batch.Logged): BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Logged, UsingPart.empty, false, QueryOptions.empty)
  }

  def logged: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Logged, UsingPart.empty, false, QueryOptions.empty)
  }

  def timestamp(stamp: Long): BatchQuery[Unspecified] = {
    apply().timestamp(stamp)
  }

  def unlogged: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Unlogged, UsingPart.empty, false, QueryOptions.empty)
  }

  def counter: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Counter, UsingPart.empty, false, QueryOptions.empty)
  }
}

