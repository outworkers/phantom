/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.builder.query

import java.util.{Iterator => JavaIterator, List => JavaList}

import com.datastax.driver.core._
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.{LimitBound, Unlimited}
import com.outworkers.phantom.connectors.KeySpace

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

trait RecordResult[R] {

  def result: ResultSet

  def pagingState: PagingState = result.getExecutionInfo.getPagingState
}

case class ListResult[R](records: List[R], result: ResultSet) extends RecordResult[R]

object ListResult {
  def apply[R](res: ResultSet, records: List[R]): ListResult[R] = ListResult(records, res)

  def apply[R](rec: (ResultSet, List[R])): ListResult[R] = apply(rec._2, rec._1)
}

case class IteratorResult[R](records: Iterator[R], result: ResultSet) extends RecordResult[R]

trait ExecutableStatement extends CassandraOperations {

  type Modifier = Statement => Statement

  def options: QueryOptions

  def qb: CQLQuery

  def queryString: String = qb.terminate.queryString

  def statement()(implicit session: Session): Statement = {
    new SimpleStatement(qb.terminate.queryString)
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
    * @param ec The implicit Scala execution context.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future()(
    implicit session: Session,
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
    * @param executor The implicit Scala executor.
    * @return An asynchronous Scala future wrapping the Datastax result set.
    */
  def future(modifyStatement: Modifier)(
    implicit session: Session,
    executor: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(modifyStatement(statement))
  }
}

private[this] object SequentialFutures {
    def sequencedTraverse[
    A,
    B,
    M[X] <: TraversableOnce[X]
  ](in: M[A])(fn: A => ScalaFuture[B])(implicit
    executor: ExecutionContextExecutor,
    cbf: CanBuildFrom[M[A], B, M[B]]
  ): ScalaFuture[M[B]] = {
    in.foldLeft(ScalaFuture.successful(cbf(in))) { (fr, a) =>
      for (r <- fr; b <- fn(a)) yield r += b
    }.map(_.result())
  }
}

class ExecutableStatementList[
  M[X] <: TraversableOnce[X]
](val queries: M[CQLQuery])(
  implicit cbf: CanBuildFrom[M[CQLQuery], CQLQuery, M[CQLQuery]]
) extends CassandraOperations {

  def add(appendable: M[CQLQuery]): ExecutableStatementList[M] = {
    val builder = cbf(queries)
    for (q <- appendable) builder += q
    new ExecutableStatementList(builder.result())
  }

  def ++(appendable: M[CQLQuery]): ExecutableStatementList[M] = add(appendable)

  def ++(st: ExecutableStatementList[M]): ExecutableStatementList[M] = add(st.queries)

  /** Transforms a `TraversableOnce[A]` into a `Future[TraversableOnce[B]]` using the provided function `A => Future[B]`.
    *  This is useful for performing a parallel map. For example, to apply a function to all items of a list
    *  in parallel:
    *
    *  {{{
    *    val myFutureList = Future.traverse(myList)(x => Future(myFunc(x)))
    *  }}}
    */

  def future()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    fbf: CanBuildFrom[Nothing, ScalaFuture[ResultSet], M[ScalaFuture[ResultSet]]],
    ebf: CanBuildFrom[M[ScalaFuture[ResultSet]], ResultSet, M[ResultSet]]
  ): ScalaFuture[M[ResultSet]] = {

    val builder = fbf()

    for (q <- queries) builder += scalaQueryStringExecuteToFuture(new SimpleStatement(q.terminate.queryString))

    ScalaFuture.sequence(builder.result())(ebf, ec)
  }

  def sequentialFuture()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[M[CQLQuery], ResultSet, M[ResultSet]]
  ): ScalaFuture[M[ResultSet]] = {
    SequentialFutures.sequencedTraverse(queries) {
      q => scalaQueryStringExecuteToFuture(new SimpleStatement(q.terminate.queryString))
    }
  }
}

private[phantom] trait RootExecutableQuery[R] {

  def fromRow(r: Row): R

  protected[this] def singleResult(row: Row): Option[R] = {
    if (Option(row).isDefined) Some(fromRow(row)) else None
  }

  protected[this] def directMapper(results: JavaList[Row])(implicit cbf: CanBuildFrom[Nothing, R, List[R]]): List[R] = {

    val builder = cbf()
    val resultSize = results.size()

    builder.sizeHint(resultSize)

    var i = 0

    while (i < resultSize) {
      builder += fromRow(results.get(i))
      i += 1
    }

    builder.result()
  }

  protected[this] def directMapper(results: JavaIterator[Row]): List[R] = {
    results.asScala.map(fromRow).toList
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

  protected[this] def greedyEval(
    f: ScalaFuture[ResultSet]
  )(implicit ex: ExecutionContextExecutor): ScalaFuture[ListResult[R]] = {
    f map { r => ListResult(directMapper(r.iterator()), r) }
  }

  protected[this] def lazyEval(
    f: ScalaFuture[ResultSet]
  )(implicit ex: ExecutionContextExecutor): ScalaFuture[IteratorResult[R]] = {
    f map { r => IteratorResult(r.iterator().asScala.map(fromRow), r) }
  }

  private[phantom] def singleFetch()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Option[R]] = {
    future() map { res => singleResult(res.one) }
  }

  private[phantom] def pagination[M[X] <: TraversableOnce[X]](res: ResultSet)(
    implicit cbf: CanBuildFrom[Nothing, R, M[R]]
  ): (M[R], ResultSet) = {
    val builder = cbf()
    val count = res.getAvailableWithoutFetching
    builder.sizeHint(count)
    var i = 0
    while (i < count) {
      builder += fromRow(res.one())
      i += 1
    }
    builder.result() -> res
  }

  private[phantom] def paginate(res: ResultSet)(
    implicit cbf: CanBuildFrom[Nothing, R, List[R]]
  ): ListResult[R] = {
    val (pag, set) = pagination[List](res)
    ListResult(pag, set)
  }

  /**
   * Returns the first row from the select ignoring everything else
   *
   * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param ev The implicit limit for the query.
   * @param ec The implicit Scala execution context.
   * @return A Scala future guaranteed to contain a single result wrapped as an Option.
   */
  def one()(
    implicit session: Session,
    ev: Limit =:= Unlimited,
    ec: ExecutionContextExecutor
  ): ScalaFuture[Option[R]]

  /**
   * Returns a parsed sequence of [R]ows
   * This is not suitable for big results set
   *
   * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
   * @param ec The implicit Scala execution context.
   * @return A Scala future wrapping a list of mapped results.
   */
  def fetch()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[List[R]] = {
    future() map { r => directMapper(r.all) }
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetch(modifyStatement : Modifier)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[List[R]] = {
    future(modifyStatement) map { r => directMapper(r.all) }
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set as it will attempt to fetch the entire result set
    * as a List, circumventing pagination settings.
    *
    * Use [[paginateRecord()]] or other means if you like to deal with bigger result sets.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    future() map (r => ListResult(directMapper(r.all), r))
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord(modifyStatement: Modifier)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ListResult[R]] = {
    future(modifyStatement) map {
      set => ListResult(directMapper(set.all), set)
    }
  }

  /**
    * Returns a parsed sequence of [R]ows but paginates the results using paging state.
    * This will not consume or return the entire set of available results, it will
    * instead return an amount of records equal to the fetch size setting.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def paginateRecord()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[Nothing, R, List[R]]
  ): ScalaFuture[ListResult[R]] = future() map paginate

  /**
    * Returns a parsed sequence of [R]ows.
    * This will only fetch the amount of records defined in the fetchSize setting.
    * It will allow pagination of the inner result set as a [[scala.collection.immutable.List]].
    *
    * It will greedy evaluate the records inside a single fetch size batch as it returns a list as opposed to
    * an iterator. For a non greedy variant of the size method use [[iterator()]].
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def paginateRecord(state: PagingState)(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[Nothing, R, Iterator[R]]
  ): ScalaFuture[ListResult[R]] = future(_.setPagingState(state)) map paginate

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def paginateRecord(state: Option[PagingState])(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[Nothing, R, List[R]]
  ): ScalaFuture[ListResult[R]] = state match {
      case None => paginateRecord()
      case Some(defined) => paginateRecord(defined)
  }

  /**
    * Returns a parsed sequence of [R]ows.
    * This will only fetch the amount of records defined in the fetchSize setting.
    * It will allow pagination of the inner result set as a [[scala.collection.immutable.List]].
    *
    * It will greedy evaluate the records inside a single fetch size batch as it returns a list as opposed to
    * an iterator. For a non greedy variant of the size method use [[iterator()]].
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def paginateRecord(modifier: Modifier)(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[Nothing, R, List[R]]
  ): ScalaFuture[ListResult[R]] = future(modifier) map paginate

  /**
    * Returns a parsed iterator of [R]ows lazily evaluated. This will respect the fetch size setting
    * of a query, meaning you will need to provide a paging state to fetch records beyond the regular fetch
    * size.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping scala iterator of mapped results.
    */
  def iterator()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[IteratorResult[R]] = {
    future() map { res => IteratorResult(res.iterator().asScala.map(fromRow), res) }
  }

  /**
    * Returns a parsed iterator of [R]ows lazily evaluated. This will respect the fetch size setting
    * of a query, meaning you will need to provide a paging state to fetch records beyond the regular fetch
    * size.
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping scala iterator of mapped results.
    */
  def iterator(modifier: Modifier)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[IteratorResult[R]] = {
    future(modifier) map (r => IteratorResult(r.iterator().asScala.map(fromRow), r))
  }

  /**
    * Returns a parsed sequence of [R]ows.
    * This will only fetch the amount of records defined in the fetchSize setting.
    * It will allow pagination of the inner result set as a [[scala.collection.immutable.List]].
    *
    * It will greedy evaluate the records inside a single fetch size batch as it returns a list as opposed to
    * an iterator. For a non greedy variant of the size method use [[iterator()]].
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def iterator(state: PagingState)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[IteratorResult[R]] = {
    lazyEval(future(_.setPagingState(state)))
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def iterator(state: Option[PagingState])(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[IteratorResult[R]] = {
    state match {
      case None => lazyEval(future())
      case Some(defined) => lazyEval(future(_.setPagingState(defined)))
    }
  }
}
