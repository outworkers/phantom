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

import cats.Monad
import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.database.Database

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class DbOps[
  F[_] : Monad,
  DB <: Database[DB]
](val db: DB) {

  private[this] val defaultTimeout = 10.seconds

  import db._

  /**
    * A blocking method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def create(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(createAsync(), timeout)
  }

  /**
    * An asynchronous method that will create all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single create operation.
    */
  def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    db.autocreate().future()(db.session, db.space, ex)
  }

  /**
    * An async method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def dropAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    db.autodrop().executable().future()
  }

  /**
    * A blocking method that will drop all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single drop operation.
    */
  def drop(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(dropAsync(), timeout)
  }

  /**
    * A blocking method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @param timeout The timeout for the initialisation call.
    *                Defaults to [[com.outworkers.phantom.database.Database#defaultTimeout]]
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncate(timeout: FiniteDuration = defaultTimeout)(implicit ex: ExecutionContextExecutor): Seq[ResultSet] = {
    Await.result(truncateAsync(), timeout)
  }

  /**
    * An async method that will truncate all the tables. This is designed to prevent the
    * requirement of the implicit session to escape the enclosure of the database object.
    *
    * @return A sequence of result sets, where every result is the result of a single truncate operation.
    */
  def truncateAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    db.autotruncate().executable().future()
  }
}