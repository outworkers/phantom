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

import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution._

import scala.collection.generic.CanBuildFrom
import scala.concurrent.Future

package object dsl extends ScalaQueryContext with DefaultImports {

  implicit val futureMonad: FutureMonad[Future] = ScalaFutureImplicits.monadInstance

  implicit class ExecuteQueries[M[X] <: TraversableOnce[X]](val qc: QueryCollection[M]) extends AnyVal {
    def executable(): ExecutableStatements[Future, M] = {
      new ExecutableStatements[Future, M](qc)(futureMonad, promiseInterface.adapter)
    }

    def future()(implicit session: Session,
      fbf: CanBuildFrom[M[Future[ResultSet]], Future[ResultSet], M[Future[ResultSet]]],
      ebf: CanBuildFrom[M[Future[ResultSet]], ResultSet, M[ResultSet]]
    ): Future[M[ResultSet]] = executable().future()
  }

  /**
    * Method that allows executing a simple query straight from text, by-passing the entire mapping layer
    * but leveraging the execution layer.
    * @param str The input [[CQLQuery]] to execute.
    * @param options The [[QueryOptions]] to pass alongside the query.
    * @return A future wrapping a database result set.
    */
  def cql(
    str: CQLQuery,
    options: QueryOptions
  ): QueryInterface[Future] = new QueryInterface[Future]()(promiseInterface.adapter) {
    override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(str, options)
  }

  /**
    * Method that allows executing a simple query straight from text, by-passing the entire mapping layer
    * but leveraging the execution layer.
    * @param str The input [[CQLQuery]] to execute.
    * @param options The [[QueryOptions]] to pass alongside the query.
    * @return A future wrapping a database result set.
    */
  def cql(
    str: String,
    options: QueryOptions = QueryOptions.empty
  ): QueryInterface[Future] = cql(CQLQuery(str), options)
}
