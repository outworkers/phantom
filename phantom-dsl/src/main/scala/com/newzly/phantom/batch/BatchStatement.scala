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
import com.newzly.phantom.query.ExecutableStatement
import com.newzly.phantom.CassandraResultSetOperations
import com.twitter.util.{ Future => TwitterFuture }

private [phantom] class BatchableStatement(val executable: ExecutableStatement)

sealed trait BatchQueryListTrait extends CassandraResultSetOperations {
  protected[this] lazy val statements: Iterator[BatchableStatement] =  Iterator.empty
  def future()(implicit session: Session): ScalaFuture[ResultSet]
  def execute()(implicit session: Session): TwitterFuture[ResultSet]
  def qbList: Iterator[BatchableStatement]
}

/**
 * !!! Attention !!!
 * This class is not meant to be used for concurrent operations on the same row inside one batch.
 * In order to have concurrent operation on the same row in the same batch, custom timesatmps needs to be inserted
 * on each statement. This is not in the scope of this class.(for now)
 */
class BatchStatement(val qbList: Iterator[BatchableStatement] = Iterator.empty) extends BatchQueryListTrait {

  final def apply(list: Iterator[BatchableStatement] = Iterator.empty) = {
    new BatchStatement(list)
  }

  @implicitNotFound("SELECT queries cannot be used in a BATCH.")
  final def add[T <: ExecutableStatement <% BatchableStatement](statement: => T): BatchStatement = {
     apply(qbList ++ Iterator(statement))
  }

  def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    val batch = new DatastaxBatchStatement()
    for (s <- qbList) {
      batch.add(s.executable.qb)
    }
    scalaStatementToFuture(batch)
  }

  def execute()(implicit session: Session): TwitterFuture[ResultSet] = {
    val batch = new DatastaxBatchStatement()
    for (s <- qbList) {
      batch.add(s.executable.qb)
    }
    twitterStatementToFuture(batch)
  }
}

object BatchStatement {
  def apply(): BatchStatement = new BatchStatement()
  def apply(statements: Iterator[BatchableStatement]) = new BatchStatement(statements)
}



