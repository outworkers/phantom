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

import com.datastax.driver.core.{Session, SimpleStatement, Statement}
import com.google.common.util.concurrent.ListenableFuture
import com.outworkers.phantom.{Manager, ResultSet}
import com.outworkers.phantom.builder.batch.BatchWithQuery
import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContextExecutor

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

  def fromGuava(in: ExecutableCqlQuery)(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[ResultSet] = fromGuava(in.statement())

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

class ExecutableStatements[
  F[_],
  M[X] <: TraversableOnce[X]
](val queryCol: QueryCollection[M])(
  implicit fMonad: FutureMonad[F],
  adapter: GuavaAdapter[F]
) {
  def sequencedTraverse[A, B](in: M[A])(fn: A => F[B])(
    implicit cbf: CanBuildFrom[M[A], B, M[B]],
    ctx: ExecutionContextExecutor
  ): F[M[B]] = {
    in.foldLeft(fMonad.pure(cbf())) { (fr, a) =>
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
  def parallel[A](in: M[F[A]])(
    implicit cbf: CanBuildFrom[M[F[A]], A, M[A]],
    ctx: ExecutionContextExecutor
  ): F[M[A]] = {
    (fMonad.pure(cbf()) /: in) { (fr, fa) => fr.zipWith(fa)(_ += _) }.map(_.result())
  }

  /**
    * Method that will execute the queries in the underlying collection in parallel.
    * The queries will be constructed sequentally by the results asynchronous queries to be executed
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
    fbf: CanBuildFrom[M[F[ResultSet]], F[ResultSet], M[F[ResultSet]]],
    ebf: CanBuildFrom[M[F[ResultSet]], ResultSet, M[ResultSet]]
  ): F[M[ResultSet]] = {

    val builder = fbf()

    for (q <- queryCol.queries) {
      Manager.logger.info(s"Executing query: ${q.qb.queryString}")
      builder += adapter.fromGuava(q)
    }

    parallel(builder.result())(ebf, ec)
  }

  def sequence()(
    implicit session: Session,
    ec: ExecutionContextExecutor,
    cbf: CanBuildFrom[M[ExecutableCqlQuery], ResultSet, M[ResultSet]]
  ): F[M[ResultSet]] = {
    sequencedTraverse(queryCol.queries) { query =>
      Manager.logger.info(s"Executing query: ${query.qb.queryString}")
      adapter.fromGuava(query)
    }
  }
}
