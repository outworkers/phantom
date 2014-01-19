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
package com
package newzly

package phantom
package query

import scala.collection.JavaConverters._
import com.twitter.util.{FuturePool, Future}

import com.datastax.driver.core.{ Row, ResultSet, Session, Statement }
import com.newzly.phantom.{ CassandraResultSetOperations, CassandraTable };

trait ExecutableStatement extends CassandraResultSetOperations {

  def qb: Statement

  def execute()(implicit session: Session): Future[ResultSet] =  {
    statementExecuteToFuture(qb)
  }
}

trait ExecutableQuery[T <: CassandraTable[T, _], R] extends CassandraResultSetOperations {

  def qb: Statement
  def table: CassandraTable[T, _]
  def fromRow(r: Row): R

  def execute()(implicit session: Session): Future[ResultSet] =
    statementExecuteToFuture(qb)

  def fetchSync(implicit session: Session): Seq[R] = {
    session.execute(qb).all().asScala.toSeq.map(fromRow)
  }

  def fetch(implicit session: Session): Future[Seq[R]] = {
    statementExecuteToFuture(qb).map(_.all().asScala.toSeq.map(fromRow))
  }

  def one(implicit session: Session): Future[Option[R]] = {
    statementExecuteToFuture(qb).map(r => Option(r.one()).map(fromRow))
  }
}