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

import com.datastax.driver.core.querybuilder.{ Batch, QueryBuilder }
import com.websudos.phantom.query.{ BatchableQuery, CQLQuery, ExecutableStatement }


sealed abstract class BatchableTypes {
  type BatchableStatement = BatchableQuery[_] with ExecutableStatement
}

sealed abstract class RootBatch[X](protected[this] val qbList: Iterator[BatchableTypes#BatchableStatement] = Iterator.empty) extends CQLQuery[X] {
  self: X =>

  type BatchableStatement = BatchableTypes#BatchableStatement

  protected[phantom] val qb = create()

  protected[this] lazy val statements: Iterator[BatchableStatement] = qbList

  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): X

  protected[this] def create(): Batch

  final def add(statements: BatchableStatement*): X = {
    for (st <- statements) qb.add(st.qb)
    this
  }

  final def timestamp(t: Long): X = {
    qb.using(QueryBuilder.timestamp(t))
    this
  }
}

/**
 * !!! Attention !!!
 * This class is not meant to be used for concurrent operations on the same row inside the same batch.
 * If you are updating the same record twice or performing an update and delete of the same record in the same batch,
 * you should use timestamps to define a custom execution order.
 *
 * In order to have concurrent operation on the same row in the same batch, custom timestamp needs to be inserted
 * on each statement, using the "timestamp" method available on every batchable query(INSERT, UPDATE, DELETE).
 */
sealed class BatchStatement(qbList: Iterator[BatchableTypes#BatchableStatement] = Iterator.empty)
  extends RootBatch[BatchStatement](qbList) {

  protected[this] def create(): Batch = QueryBuilder.batch()
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): BatchStatement = new BatchStatement(sts)
}

sealed class CounterBatchStatement(override protected[this] val qbList: Iterator[BatchableTypes#BatchableStatement] = Iterator.empty)
  extends RootBatch[CounterBatchStatement](qbList) {

  protected[this] def create(): Batch = QueryBuilder.batch()
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): CounterBatchStatement = new CounterBatchStatement(sts)
}

sealed class UnloggedBatchStatement(override protected[this] val qbList: Iterator[BatchableTypes#BatchableStatement] = Iterator.empty)
  extends RootBatch[UnloggedBatchStatement](qbList) {

  protected[this] def create(): Batch = QueryBuilder.unloggedBatch()
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): UnloggedBatchStatement = new UnloggedBatchStatement(sts)
}

object BatchStatement {
  def apply(): BatchStatement = new BatchStatement()
  def apply(statements: Iterator[BatchableTypes#BatchableStatement]): BatchStatement = new BatchStatement(statements)
}

object CounterBatchStatement {
  def apply(): CounterBatchStatement = new CounterBatchStatement()
  def apply(statements: Iterator[BatchableTypes#BatchableStatement]): CounterBatchStatement = new CounterBatchStatement(statements)
}

object UnloggedBatchStatement {
  def apply(): UnloggedBatchStatement = new UnloggedBatchStatement()
  def apply(statements: Iterator[BatchableTypes#BatchableStatement]): UnloggedBatchStatement = new UnloggedBatchStatement(statements)
}


