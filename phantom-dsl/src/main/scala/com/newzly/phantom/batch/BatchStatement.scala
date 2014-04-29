/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.batch

import scala.annotation.implicitNotFound
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ BatchStatement => DatastaxBatchStatement, ResultSet, Session }
import com.newzly.phantom.CassandraResultSetOperations
import com.newzly.phantom.query.ExecutableStatement
import com.twitter.util.{ Future => TwitterFuture }

private [phantom] class BatchableStatement(val executable: ExecutableStatement)

sealed abstract class BatchQueryListTrait[X](protected[this] val qbList: Iterator[BatchableStatement] = Iterator.empty) extends CassandraResultSetOperations {
  self: X =>

  protected[this] lazy val statements: Iterator[BatchableStatement] =  Iterator.empty

  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): X

  protected[this] def create(): DatastaxBatchStatement

  final def apply(list: Iterator[BatchableStatement] = Iterator.empty): X = {
    newSubclass(list)
  }

  @implicitNotFound("SELECT, CREATE and TRUNCATE queries cannot be used in a BATCH.")
  final def add[T <: ExecutableStatement <% BatchableStatement](statement: => T): X = {
    apply(qbList ++ Iterator(statement))
  }

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    val batch = create()
    for (s <- qbList) {
      batch.add(s.executable.qb)
    }
    scalaStatementToFuture(batch)
  }

  def execute()(implicit session: Session): TwitterFuture[ResultSet] = {
    val batch = create()
    for (s <- qbList) {
      batch.add(s.executable.qb)
    }
    twitterStatementToFuture(batch)
  }
}

/**
 * !!! Attention !!!
 * This class is not meant to be used for concurrent operations on the same row inside one batch.
 * In order to have concurrent operation on the same row in the same batch, custom timestamp needs to be inserted
 * on each statement. This is not in the scope of this class.(for now)
 */
sealed class BatchStatement(qbList: Iterator[BatchableStatement] = Iterator.empty) extends BatchQueryListTrait[BatchStatement](qbList) {
  protected[this] def create(): DatastaxBatchStatement = new DatastaxBatchStatement(DatastaxBatchStatement.Type.LOGGED)
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): BatchStatement = new BatchStatement(sts)
}

sealed class CounterBatchStatement(override protected[this] val qbList: Iterator[BatchableStatement] = Iterator.empty) extends BatchQueryListTrait[CounterBatchStatement](qbList) {
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): CounterBatchStatement = new CounterBatchStatement(sts)
  protected[this] def create(): DatastaxBatchStatement = new DatastaxBatchStatement(DatastaxBatchStatement.Type.COUNTER)
}

sealed class UnloggedBatchStatement(override protected[this] val qbList: Iterator[BatchableStatement] = Iterator.empty) extends BatchQueryListTrait[UnloggedBatchStatement](qbList) {
  protected[this] def newSubclass(sts: Iterator[BatchableStatement]): UnloggedBatchStatement = new UnloggedBatchStatement(sts)
  protected[this] def create(): DatastaxBatchStatement = new DatastaxBatchStatement(DatastaxBatchStatement.Type.UNLOGGED)
}

object BatchStatement {
  def apply(): BatchStatement = new BatchStatement()
  def apply(statements: Iterator[BatchableStatement]): BatchStatement = new BatchStatement(statements)
}

object CounterBatchStatement {
  def apply(): CounterBatchStatement = new CounterBatchStatement()
  def apply(statements: Iterator[BatchableStatement]): CounterBatchStatement = new CounterBatchStatement(statements)
}

object UnloggedBatchStatement {
  def apply(): UnloggedBatchStatement = new UnloggedBatchStatement()
  def apply(statements: Iterator[BatchableStatement]): UnloggedBatchStatement = new UnloggedBatchStatement(statements)
}



