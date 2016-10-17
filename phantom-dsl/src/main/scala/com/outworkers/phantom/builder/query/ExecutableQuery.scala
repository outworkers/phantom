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
package com.outworkers.phantom.builder.query

import java.util.{List => JavaList}

import com.datastax.driver.core._
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.{LimitBound, Unlimited}
import com.outworkers.phantom.connectors.KeySpace

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

trait RecordResult[R] {

  def result: ResultSet

  def pagingState: PagingState = result.getExecutionInfo.getPagingState
}

case class ListResult[R](records: List[R], result: ResultSet) extends RecordResult[R]

case class IteratorResult[R](records: Iterator[R], result: ResultSet) extends RecordResult[R]

trait ExecutableStatement extends CassandraOperations {

  type Modifier = Statement => Statement

  def options: QueryOptions

  def qb: CQLQuery

  def queryString: String = qb.terminate().queryString

  def statement()(implicit session: Session): Statement = {
    new SimpleStatement(qb.terminate().queryString)
      .setConsistencyLevel(options.consistencyLevel.orNull)
  }

  /**
    * Default asynchronous query execution method. This will convert the underlying
    * call to Cassandra done with Google Guava ListenableFuture to a consumable
    * Scala Future that will be completed once the operation is completed on the
    * database end.
    *
    * The execution context of the transformation is provided by phantom via
    * [[com.outworkers.phantom.Manager.scalaExecutor]] and it is recommended to
    * use [[com.outworkers.phantom.dsl.context]] for operations that chain
    * database calls.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(statement)
  }

  /**
    * This will convert the underlying call to Cassandra done with Google Guava ListenableFuture to a consumable
    * Scala Future that will be completed once the operation is completed on the
    * database end.
    *
    * The execution context of the transformation is provided by phantom via
    * [[com.outworkers.phantom.Manager.scalaExecutor]] and it is recommended to
    * use [[com.outworkers.phantom.dsl.context]] for operations that chain
    * database calls.
    *
    * @param modifyStatement The function allowing to modify underlying [[Statement]]
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param executor The implicit Scala executor.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future(modifyStatement: Modifier)(
    implicit session: Session,
    keySpace: KeySpace,
    executor: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(modifyStatement(statement))
  }
}

class ExecutableStatementList(val queries: Seq[CQLQuery]) extends CassandraOperations {

  /**
   * Secondary constructor to allow passing in Sets instead of Sequences.
   * Although this may appear to be fruitless and uninteresting it a necessary evil.
   *
   * The TwitterFuture.collect method does not support passing in arbitrary collections using the Scala API
   * just as Scala.future does. Scala Futures can sequence over traversables and return a collection of the appropiate type.
   *
   * @param queries The list of CQL queries to execute.
   * @return An instance of an ExecutableStatement with the matching sequence of CQL queries.
   */
  def this(queries: Set[CQLQuery]) = this(queries.toSeq)

  def add(appendable: Seq[CQLQuery]): ExecutableStatementList = {
    new ExecutableStatementList(queries ++ appendable)
  }

  def ++(appendable: Seq[CQLQuery]): ExecutableStatementList = add(appendable)

  def ++(st: ExecutableStatementList): ExecutableStatementList = add(st.queries)

  def future()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Seq[ResultSet]] = {
    ScalaFuture.sequence(queries.map(item => {
      scalaQueryStringExecuteToFuture(new SimpleStatement(item.terminate().queryString))
    }))
  }
}

private[phantom] trait RootExecutableQuery[R] {

  def fromRow(r: Row): R

  protected[this] def singleResult(row: Row): Option[R] = {
    if (Option(row).isDefined) Some(fromRow(row)) else None
  }

  protected[this] def directMapper(results: JavaList[Row]): List[R] = {
    List.tabulate(results.size())(index => fromRow(results.get(index)))
  }
}

/**
 * An ExecutableQuery implementation, meant to retrieve results from Cassandra.
 * This provides the root implementation of a Select query.
 *
 * @tparam T The class owning the table.
 * @tparam R The record type to store.
 */
trait ExecutableQuery[T <: CassandraTable[T, _], R, Limit <: LimitBound]
  extends ExecutableStatement with RootExecutableQuery[R] {

  def fromRow(r: Row): R

  private[phantom] def singleFetch()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Option[R]] = {
    future() map { res => singleResult(res.one) }
  }

  /**
   * Returns the first row from the select ignoring everything else
   *
   * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param ev The implicit limit for the query.
   * @param ec The implicit Scala execution context.
   * @return A Scala future guaranteed to contain a single result wrapped as an Option.
   */
  def one()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: Limit =:= Unlimited,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Option[R]]

  /**
   * Returns a parsed sequence of [R]ows
   * This is not suitable for big results set
   *
   * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param ec The implicit Scala execution context.
   * @return A Scala future wrapping a list of mapped results.
   */
  def fetch()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[List[R]] = {
    future() map { resultSet => directMapper(resultSet.all) }
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetch(state: PagingState)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[List[R]] = {
    future(_.setPagingState(state)) map {
      resultSet => directMapper(resultSet.all)
    }
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetch(modifyStatement : Modifier)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[List[R]] = {
    future(modifyStatement) map {
      resultSet => directMapper(resultSet.all)
    }
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    future() map (resultSet => ListResult(directMapper(resultSet.all), resultSet))
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord(state: PagingState)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    future(st => st.setPagingState(state)) map {
      set => ListResult(directMapper(set.all), set)
    }
  }

  def fetchRecord(state: Option[PagingState])(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    state.fold(future().map {
      set => ListResult(directMapper(set.all), set)
    }) (state => future(_.setPagingState(state)) map {
      set => ListResult(directMapper(set.all), set)
    })
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord(modifyStatement: Modifier)(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    future(modifyStatement) map {
      set => ListResult(directMapper(set.all), set)
    }
  }

  /**
   * Returns a parsed iterator of [R]ows
   *
   * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param keySpace The implicit keySpace definition provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param ec The implicit Scala execution context.
   * @return A Scala future wrapping scala iterator of mapped results.
   */
  def iterator()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Iterator[R]] = {
    future() map { _.iterator().asScala.map(fromRow) }
  }

  def iteratorRecord()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[IteratorResult[R]] = {
    future() map { result => IteratorResult(result.iterator().asScala.map(fromRow), result) }
  }
}
