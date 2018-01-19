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
package com.outworkers.phantom.ops

import com.datastax.driver.core.Session
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.CreateQuery.DelegatedCreateQuery
import com.outworkers.phantom.builder.query.execution._
import com.outworkers.phantom.database.Database

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContextExecutor

abstract class DbOps[
  P[_],
  F[_] : FutureMonad,
  DB <: Database[DB],
  Timeout
](val db: Database[DB])(implicit interface: PromiseInterface[P, F]) {

  import db._

  def executeCreateQuery(query: DelegatedCreateQuery)(
    implicit ctx: ExecutionContextExecutor,
    session: Session
  ): F[Seq[ResultSet]] = {

    implicit val adapter: GuavaAdapter[F] = interface.adapter

    for {
      tableCreationQuery <- adapter.fromGuava(query.executable)
      secondaryIndexes <- new ExecutableStatements(query.indexList).future()
      sasiIndexes <- new ExecutableStatements(query.sasiIndexes).future()
    } yield Seq(tableCreationQuery) ++ secondaryIndexes ++ sasiIndexes
  }

  def execute[M[X] <: TraversableOnce[X]](col: QueryCollection[M])(
    implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
  ): ExecutableStatements[F, M]

  def defaultTimeout: Timeout

  def await[T](f: F[T], timeout: Timeout = defaultTimeout): T

  /**
    * A blocking method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def create(timeout: Timeout = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[Seq[ResultSet]] = {
    await(createAsync(), timeout)
  }

  /**
    * An asynchronous method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def createAsync()(
    implicit ex: ExecutionContextExecutor
  ): F[Seq[Seq[ResultSet]]] = {
    ExecutionHelper.sequencedTraverse(tables.map(_.create.ifNotExists().delegate))(executeCreateQuery)
  }

  /**
    * An async method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def dropAsync()(implicit ex: ExecutionContextExecutor): F[Seq[ResultSet]] = {
    execute(db.autodrop()).future()
  }

  /**
    * A blocking method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def drop(timeout: Timeout = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    await(dropAsync(), timeout)
  }

  /**
    * A blocking method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncate(timeout: Timeout = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    await(truncateAsync(), timeout)
  }

  /**
    * An async method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncateAsync()(implicit ex: ExecutionContextExecutor): F[Seq[ResultSet]] = {
    execute(db.autotruncate()).future()
  }
}