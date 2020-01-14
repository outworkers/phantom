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
package com.outworkers.phantom.builder.query.execution

import com.datastax.driver.core.{Session, SimpleStatement, Statement}
import com.google.common.util.concurrent.ListenableFuture
import com.outworkers.phantom.{Manager, ResultSet}
import com.outworkers.phantom.builder.batch.BatchWithQuery
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.concurrent.ExecutionContextExecutor
import scala.collection.compat._

trait GuavaAdapter[F[_]] {

  def executeBatch(batch: BatchWithQuery)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet] = {
    Manager.logger.info(s"Executing BATCH query ${batch.debugString}")
    fromGuava(batch.statement)
  }

  def fromGuava[T](source: ListenableFuture[T])(
    implicit executor: ExecutionContextExecutor
  ): F[T]

  def fromGuava(in: ExecutableCqlQuery, modifier: Option[Statement => Statement] = None)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet] = fromGuava(modifier.getOrElse(identity[Statement] _).apply(in.statement()))

  def fromGuava(in: Statement)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet]

  def fromGuava(qb: CQLQuery)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet] = {
    fromGuava(new SimpleStatement(qb.terminate.queryString))
  }
}

object ExecutionHelper {
  def sequencedTraverse[
    F[_],
    M[X] <: IterableOnce[X],
    A,
    B
  ](in: M[A])(fn: A => F[B])(
    implicit cbf: Factory[B, M[B]],
    ctx: ExecutionContextExecutor,
    fMonad: FutureMonad[F]
  ): F[M[B]] = {
    in.iterator.foldLeft(fMonad.pure(cbf.newBuilder)) { (fr, a) =>
      for (r <- fr; b <- fn(a)) yield r += b
    }.map(_.result())
  }

  /**
    * Simple version of `Future.traverse`. Asynchronously transforms a `TraversableOnce[Future[A]]`
    *  into a `Future[TraversableOnce[A]]`. Useful for reducing many `Future`s into a single `Future`.
    *
    * @tparam A        the type of the value inside the Futures
    * @param in        the `TraversableOnce` of Futures which will be sequenced
    * @return          the `Future` of the `TraversableOnce` of results
    */
  def parallel[
    F[_],
    M[X] <: IterableOnce[X],
    A](in: M[F[A]])(
    implicit cbf: Factory[A, M[A]],
    fMonad: FutureMonad[F],
    ctx: ExecutionContextExecutor
  ): F[M[A]] = {
    in.iterator.foldLeft(fMonad.pure(cbf.newBuilder)) {
      (fr, fa) => fr.zipWith(fa)(_ += _)
    } map(_.result())
  }
}

class ExecutableStatements[
  F[_],
  M[X] <: IterableOnce[X]
](val queryCol: QueryCollection[M])(
  implicit fMonad: FutureMonad[F],
  adapter: GuavaAdapter[F]
) {

  /**
    * Method that will execute the queries in the underlying collection in parallel.
    * The queries will be constructed sequentially by the results asynchronous queries to be executed
    * will be executed in parallel by default using this method.
    *
    * @param session The cassandra session to execute the query collection against.
    * @param ec The execution context in which to execute the queries.
    * @param fbf A builder object to allow convert the queries into DB operations.
    * @param ebf A builder object to allow dealing with A List double higher kinded restricted collections.
    * @return A future of type F wrapping a collection of type M.
    */
  def future()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    fbf: Factory[F[ResultSet], M[F[ResultSet]]],
    ebf: Factory[ResultSet, M[ResultSet]]
  ): F[M[ResultSet]] = {

    val builder = fbf.newBuilder

    for (q <- queryCol.queries.iterator) {
      Manager.logger.info(s"Executing query: ${q.qb.queryString}")
      builder += adapter.fromGuava(q)
    }

    ExecutionHelper.parallel(builder.result())(ebf, fMonad, ec)
  }

  def sequence()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: Factory[ResultSet, M[ResultSet]]
  ): F[M[ResultSet]] = {
    ExecutionHelper.sequencedTraverse(queryCol.queries) { query =>
      Manager.logger.info(s"Executing query: ${query.qb.queryString}")
      adapter.fromGuava(query)
    }
  }
}
