/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom.batch

import com.datastax.driver.core.{QueryOptions => _, _}
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

class BatchType(val batch: String)

object BatchType {
  case object Logged extends BatchType(CQLSyntax.Batch.Logged)
  case object Unlogged extends BatchType(CQLSyntax.Batch.Unlogged)
  case object Counter extends BatchType(CQLSyntax.Batch.Counter)

}

sealed class BatchQuery[Status <: ConsistencyBound](
  val iterator: Iterator[_ <: Statement],
  batchType: BatchType,
  usingPart: UsingPart = UsingPart.empty,
  added: Boolean = false,
  override val options: QueryOptions
) extends ExecutableStatement {

  override def future()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(makeBatch())
  }

  def initBatch(): BatchStatement = batchType match {
    case BatchType.Logged => new BatchStatement()
    case BatchType.Unlogged => new BatchStatement(BatchStatement.Type.UNLOGGED)
    case BatchType.Counter => new BatchStatement(BatchStatement.Type.COUNTER)
  }

  def makeBatch()(implicit session: Session): Statement = {
    val batch = initBatch()

    for (st <- iterator) {
      batch.add(st)
    }

    options.consistencyLevel match {
      case Some(level) => batch.setConsistencyLevel(level)
      case None => batch
    }

  }


  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  final def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): BatchQuery[Specified] = {
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
      iterator ++ Iterator(query.statement()),
      batchType,
      usingPart,
      added,
      options
    )
  }

  def add(queries: Batchable with ExecutableStatement*)(implicit session: Session): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries.map(_.statement()).iterator,
      batchType,
      usingPart,
      added,
      options
    )
  }

  def add(queries: Iterator[Batchable with ExecutableStatement])(implicit session: Session): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries.map(_.statement()),
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

