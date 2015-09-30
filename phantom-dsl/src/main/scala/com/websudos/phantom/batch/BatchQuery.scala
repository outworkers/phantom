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

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{Batchable, CQLQuery, ExecutableStatement}
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed class BatchQuery(val init: CQLQuery, added: Boolean = false) extends ExecutableStatement {

  def add(queries: Batchable with ExecutableStatement*): BatchQuery = {

    val chain = queries.foldLeft(init) {
      (builder, query) => builder.forcePad.append(query.qb.terminate())
    }

    new BatchQuery(chain)
  }

  def timestamp(stamp: Long) = new BatchQuery(QueryBuilder.timestamp(init, stamp.toString))

  def terminate: BatchQuery = new BatchQuery(QueryBuilder.Batch.applyBatch(init), true)

  override def qb: CQLQuery = if (added) {
    init
  } else {
    terminate.qb
  }
}

private[phantom] trait Batcher {

  def apply(batchType: String = CQLSyntax.Batch.Logged): BatchQuery = {
    new BatchQuery(QueryBuilder.Batch.batch(batchType))
  }

  def logged: BatchQuery = {
    new BatchQuery(QueryBuilder.Batch.batch(""))
  }

  def timestamp(stamp: Long) = {
    apply().timestamp(stamp)
  }

  def unlogged: BatchQuery = {
    new BatchQuery(QueryBuilder.Batch.batch(CQLSyntax.Batch.Unlogged))
  }

  def counter: BatchQuery = {
    new BatchQuery(QueryBuilder.Batch.batch(CQLSyntax.Batch.Counter))
  }
}

