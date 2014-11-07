/*
 * Copyright 2013 websudos ltd.
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


