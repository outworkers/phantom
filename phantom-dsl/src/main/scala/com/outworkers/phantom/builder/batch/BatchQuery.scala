/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
package com.outworkers.phantom.builder.batch

import com.datastax.driver.core.{BatchStatement, ConsistencyLevel, Session, Statement}
import com.outworkers.phantom.builder.query.{Batchable, _}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}
import com.outworkers.phantom.connectors.SessionAugmenterImplicits
import org.joda.time.DateTime

import scala.annotation.implicitNotFound
import scala.collection.compat._

case class BatchWithQuery(
  statement: Statement,
  queries: String,
  batchType: BatchType
) {
  val debugString = s"BEGIN BATCH ${batchType.batch} ($queries) APPLY BATCH;"
}

class BatchType(val batch: String)

object BatchType {
  case object Logged extends BatchType(CQLSyntax.Batch.Logged)
  case object Unlogged extends BatchType(CQLSyntax.Batch.Unlogged)
  case object Counter extends BatchType(CQLSyntax.Batch.Counter)
}

sealed case class BatchQuery[Status <: ConsistencyBound](
  iterator: Iterator[Batchable],
  batchType: BatchType,
  usingPart: UsingPart = UsingPart.empty,
  options: QueryOptions
) extends SessionAugmenterImplicits {

  def initBatch(): BatchStatement = batchType match {
    case BatchType.Logged => new BatchStatement(BatchStatement.Type.LOGGED)
    case BatchType.Unlogged => new BatchStatement(BatchStatement.Type.UNLOGGED)
    case BatchType.Counter => new BatchStatement(BatchStatement.Type.COUNTER)
  }

  def makeBatch()(implicit session: Session): BatchWithQuery = {
    val batch = initBatch()

    val builder = List.newBuilder[String]

    for (st <- iterator) {
      builder += st.executableQuery.qb.queryString
      batch.add(st.executableQuery.statement())
    }

    val strings = builder.result()
    BatchWithQuery(batch, strings.mkString("\n"), batchType)
  }

  def queryString()(implicit session: Session): String = makeBatch().debugString

  @implicitNotFound("A ConsistencyLevel was already specified for this query.")
  final def consistencyLevel_=(level: ConsistencyLevel)(
    implicit ev: Status =:= Unspecified,
    session: Session
  ): BatchQuery[Specified] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  def add(queries: Batchable*): BatchQuery[Status] = {
    copy(iterator = iterator ++ queries.iterator)
  }

  def add(query: Option[Batchable]): BatchQuery[Status] = {
    query match {
      case Some(value) => copy(iterator = iterator ++ Iterator(value))
      case None => this
    }
  }

  def add[M[X] <: IterableOnce[X], Y <: Batchable](queries: M[Y])(
    implicit cbf: Factory[Batchable, Iterator[Batchable]]
  ): BatchQuery[Status] = {
    val builder = cbf.newBuilder
    queries.foreach(builder +=)
    copy(iterator = iterator ++ builder.result())
  }

  /**
    * Adds the statement of another query to the statements of this batch query.
    * @param batch A batch query.
    * @return A batch query with the same consistencyLevel as the original query.
    */
  def add(batch: BatchQuery[_]): BatchQuery[Status] = {
    copy(this.iterator ++ batch.iterator)
  }

  def timestamp(time: DateTime): BatchQuery[Status] = {
    timestamp(time.getMillis)
  }

  def timestamp(stamp: Long): BatchQuery[Status] = {
    copy(usingPart = usingPart append QueryBuilder.microstamp(stamp * 1000))
  }
}

private[phantom] trait Batcher {

  def apply(batchType: BatchType = BatchType.Logged): BatchQuery[Unspecified] = {
    BatchQuery(Iterator.empty, batchType, UsingPart.empty, QueryOptions.empty)
  }

  def logged: BatchQuery[Unspecified] = {
    BatchQuery(Iterator.empty, BatchType.Logged, UsingPart.empty, QueryOptions.empty)
  }

  def timestamp(stamp: Long): BatchQuery[Unspecified] = {
    apply().timestamp(stamp)
  }

  def unlogged: BatchQuery[Unspecified] = {
    BatchQuery(Iterator.empty, BatchType.Unlogged, UsingPart.empty, QueryOptions.empty)
  }

  def counter: BatchQuery[Unspecified] = {
    BatchQuery(Iterator.empty, BatchType.Counter, UsingPart.empty, QueryOptions.empty)
  }
}

