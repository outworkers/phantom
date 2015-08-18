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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.batch

import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.{Batch, QueryBuilder => DatastaxBuilder}
import com.twitter.util.{Future => TwitterFuture}
import com.websudos.phantom.builder.query._
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound
import scala.concurrent.{Future => ScalaFuture}

class BatchType(val batch: String)

object BatchType {
  case object Logged extends BatchType(CQLSyntax.Batch.Logged)
  case object Unlogged extends BatchType(CQLSyntax.Batch.Unlogged)
  case object Counter extends BatchType(CQLSyntax.Batch.Counter)

}

sealed class BatchQuery[Status <: ConsistencyBound](
  val iterator: Iterator[CQLQuery],
  batchType: BatchType,
  usingPart: UsingPart = UsingPart.empty,
  added: Boolean = false,
  override val consistencyLevel: Option[ConsistencyLevel]
) extends ExecutableStatement {

  override def future()(implicit session: Session, keySpace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(makeBatch())
  }

  override def execute()(implicit session: Session, keySpace: KeySpace): TwitterFuture[ResultSet] = {
    twitterQueryStringExecuteToFuture(makeBatch())
  }

  def initBatch(): Batch = batchType match {
    case BatchType.Logged => DatastaxBuilder.batch()
    case BatchType.Unlogged => DatastaxBuilder.unloggedBatch()
    case BatchType.Counter => DatastaxBuilder.batch()
  }

  def makeBatch()(implicit session: Session): Statement = {
    val batch = initBatch()

    for (st <- iterator) {
      batch.add(new SimpleStatement(st.queryString))
    }

    consistencyLevel match {
      case Some(level) => batch.setConsistencyLevel(level)
      case None => batch
    }

  }

  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  final def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): BatchQuery[Specified] = {
    if (session.v3orNewer) {
      new BatchQuery[Specified](
        iterator,
        batchType,
        usingPart,
        added,
        Some(level)
      )
    } else {
      new BatchQuery[Specified](
        iterator,
        batchType,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        added,
        Some(level)
      )
    }
  }

  def add(query: Batchable with ExecutableStatement): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ Iterator(query.qb),
      batchType,
      usingPart,
      added,
      consistencyLevel
    )
  }

  def add(queries: Batchable with ExecutableStatement*): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries.map(_.qb).iterator,
      batchType,
      usingPart,
      added,
      consistencyLevel
    )
  }

  def add(queries: Iterator[Batchable with ExecutableStatement]): BatchQuery[Status] = {
    new BatchQuery(
      iterator ++ queries.map(_.qb),
      batchType,
      usingPart,
      added,
      consistencyLevel
    )
  }

  def timestamp(stamp: Long): BatchQuery[Status] = {
    new BatchQuery(
      iterator,
      batchType,
      usingPart append QueryBuilder.timestamp(stamp.toString),
      added,
      consistencyLevel
    )
  }

  override def qb: CQLQuery = CQLQuery.empty
}

private[phantom] trait Batcher {

  def apply(batchType: String = CQLSyntax.Batch.Logged): BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Logged, UsingPart.empty, false, None)
  }

  def logged: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Logged, UsingPart.empty, false, None)
  }

  def timestamp(stamp: Long): BatchQuery[Unspecified] = {
    apply().timestamp(stamp)
  }

  def unlogged: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Unlogged, UsingPart.empty, false, None)
  }

  def counter: BatchQuery[Unspecified] = {
    new BatchQuery(Iterator.empty, BatchType.Counter, UsingPart.empty, false, None)
  }
}

