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
package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.{PagingState, Session, Statement}
import com.outworkers.phantom.builder.{LimitBound, Unlimited}
import com.outworkers.phantom.{CassandraTable, ResultSet, Row}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContextExecutor

abstract class ResultQueryInterface[
  F[_],
  T <: CassandraTable[T, _],
  R,
  Limit <: LimitBound
]()(implicit fMonad: FutureMonad[F], adapter: GuavaAdapter[F]) extends QueryInterface[F] {

  def fromRow(r: Row): R

  protected[this] def singleResult(row: Option[Row]): Option[R] = {
    row map fromRow
  }

  protected[this] def flattenedOption[Inner](
    row: Option[Row]
  )(implicit ev: R <:< Option[Inner]): Option[Inner] = {
    row flatMap fromRow
  }

  protected[this] def directMapper(
    results: Iterator[Row]
  ): List[R] = results.map(fromRow).toList

  protected[this] def greedyEval(
    f: F[ResultSet]
  )(implicit ctx: ExecutionContextExecutor): F[ListResult[R]] = {
    f map { r => ListResult(directMapper(r.iterate()), r) }
  }

  protected[this] def lazyEval(
    f: F[ResultSet]
  )(implicit ctx: ExecutionContextExecutor): F[IteratorResult[R]] = {
    f map { r => IteratorResult(r.iterate().map(fromRow), r) }
  }

  private[phantom] def optionalFetch[Inner](source: F[ResultSet])(
    implicit ec: ExecutionContextExecutor,
    ev: R <:< Option[Inner]
  ): F[Option[Inner]] = {
    source map { res => flattenedOption(res.value()) }
  }

  private[phantom] def singleFetch(source: F[ResultSet])(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): F[Option[R]] = {
    source map { res => singleResult(res.value()) }
  }

  /**
    * Paginates a [[ResultSet]] by manually parsing [[Row]] into the Record type [[R]]
    * by applying the [[fromRow]] method up to [[com.datastax.driver.core.ResultSet#getAvailableWithoutFetching]].
    * This ensures we do not map more records that already retrieved from the database, thereby honouring the
    * fetch size set on the underlying query.
    * @param res The underlying [[ResultSet]] used.
    * @param cbf The [[CanBuildFrom]] instance used to build the final collection.
    * @tparam Col The higher kinded type used to abstract over the implementation of the resulting collection.
    * @return A tuple of the mapped collection and the original [[ResultSet]].
    */
  private[phantom] def pagination[Col[X] <: TraversableOnce[X]](res: ResultSet)(
    implicit cbf: CanBuildFrom[Nothing, R, Col[R]]
  ): (Col[R], ResultSet) = {
    val builder = cbf()
    val count = res.getAvailableWithoutFetching
    builder.sizeHint(count)
    var i = 0
    while (i < count) {
      builder += fromRow(res.value().get)
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
  ): F[Option[R]]

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
  ): F[List[R]] = {
    future() map (_.allRows().map(fromRow))
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetch(modifyStatement: Statement => Statement)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): F[List[R]] = {
    future(modifyStatement) map (_.allRows().map(fromRow))
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
  ): F[ListResult[R]] = {
    future() map (r => ListResult(r.allRows().map(fromRow), r))
  }

  /**
    * Returns a parsed sequence of [R]ows
    * This is not suitable for big results set
    *
    * @param session The implicit session provided by a [[com.outworkers.phantom.connectors.Connector]].
    * @param ec The implicit Scala execution context.
    * @return A Scala future wrapping a list of mapped results.
    */
  def fetchRecord(modifyStatement: Statement => Statement)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): F[ListResult[R]] = {
    future(modifyStatement) map {
      set => ListResult(set.allRows().map(fromRow), set)
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
  ): F[ListResult[R]] = future() map paginate

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
  ): F[ListResult[R]] = future(_.setPagingState(state)) map paginate

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
  ): F[ListResult[R]] = state match {
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
  def paginateRecord(modifier: Statement => Statement)(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[Nothing, R, List[R]]
  ): F[ListResult[R]] = future(modifier) map paginate

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
  ): F[IteratorResult[R]] = {
    future() map { res => IteratorResult(res.iterate().map(fromRow), res) }
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
  def iterator(modifier: Statement => Statement)(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): F[IteratorResult[R]] = {
    future(modifier) map (r => IteratorResult(r.iterate().map(fromRow), r))
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
  ): F[IteratorResult[R]] = {
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
  ): F[IteratorResult[R]] = {
    state match {
      case None => lazyEval(future())
      case Some(defined) => lazyEval(future(_.setPagingState(defined)))
    }
  }

}
