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
package com.outworkers.phantom

import cats.Monad
import cats.instances.FutureInstances
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution._

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContextExecutor, Future}

package object dsl extends ScalaQueryContext with DefaultImports with FutureInstances {

  def cql(
    str: CQLQuery,
    options: QueryOptions = QueryOptions.empty
  ): QueryInterface[Future] = new QueryInterface[Future]() {
    override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(str, options)
  }

  def cql(str: String): QueryInterface[Future] = cql(CQLQuery(str))

  implicit class ExecuteQueries[M[X] <: TraversableOnce[X]](val qc: QueryCollection[M]) extends AnyVal {
    def executable()(
      implicit ctx: ExecutionContextExecutor
    ): ExecutableStatements[Future, M] = {
      implicit val fMonad: Monad[Future] = catsStdInstancesForFuture(ctx)
      new ExecutableStatements[Future, M](qc)
    }

    def future()(implicit session: Session,
      fbf: CanBuildFrom[M[Future[ResultSet]], Future[ResultSet], M[Future[ResultSet]]],
      ebf: CanBuildFrom[M[Future[ResultSet]], ResultSet, M[ResultSet]]
    ): Future[M[ResultSet]] = executable().future()
  }
}
