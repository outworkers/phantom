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

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.websudos.phantom.builder.query._
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}

import scala.annotation.implicitNotFound

sealed class BatchQuery[Status <: ConsistencyBound](
  val init: CQLQuery,
  usingPart: UsingPart = UsingPart.empty,
  added: Boolean = false,
  override val consistencyLevel: Option[ConsistencyLevel]

) extends ExecutableStatement {


  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  final def consistencyLevel_=(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified, session: Session): BatchQuery[Specified] = {
    if (session.v3orNewer) {
      new BatchQuery[Specified](init, usingPart, added, Some(level))
    } else {
      new BatchQuery[Specified](
        init,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        added,
        Some(level)
      )
    }
  }

  def add(queries: Batchable with ExecutableStatement*): BatchQuery[Status] = {

    val chain = queries.foldLeft(init) {
      (builder, query) => builder.forcePad.append(query.qb.terminate())
    }

    new BatchQuery(chain, usingPart, added, consistencyLevel)
  }

  def timestamp(stamp: Long): BatchQuery[Status] = {
    new BatchQuery(QueryBuilder.timestamp(init, stamp.toString), usingPart, added, consistencyLevel)
  }

  def terminate: BatchQuery[Status] = {
    new BatchQuery(QueryBuilder.Batch.applyBatch(init), UsingPart.empty, true, consistencyLevel)
  }

  override def qb: CQLQuery = {
    if (added) {
      init
    } else {
      terminate.qb
    }
  }
}

private[phantom] trait Batcher {

  def apply(batchType: String = CQLSyntax.Batch.Logged): BatchQuery[Unspecified] = {
    new BatchQuery(QueryBuilder.Batch.batch(batchType), UsingPart.empty, false, None)
  }

  def logged: BatchQuery[Unspecified] = {
    new BatchQuery(QueryBuilder.Batch.batch(""), UsingPart.empty, false, None)
  }

  def timestamp(stamp: Long): BatchQuery[Unspecified] = {
    apply().timestamp(stamp)
  }

  def unlogged: BatchQuery[Unspecified] = {
    new BatchQuery(QueryBuilder.Batch.batch(CQLSyntax.Batch.Unlogged), UsingPart.empty, false, None)
  }

  def counter: BatchQuery[Unspecified] = {
    new BatchQuery(QueryBuilder.Batch.batch(CQLSyntax.Batch.Counter), UsingPart.empty, false, None)
  }
}

