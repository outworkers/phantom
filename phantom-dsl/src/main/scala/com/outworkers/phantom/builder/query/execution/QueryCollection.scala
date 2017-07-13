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

import com.outworkers.phantom.builder.query.engine.CQLQuery

import scala.collection.generic.CanBuildFrom

class QueryCollection[M[X] <: TraversableOnce[X]](val queries: M[CQLQuery])(
  implicit cbf: CanBuildFrom[M[CQLQuery], CQLQuery, M[CQLQuery]]
) {

  def isEmpty: Boolean = queries.isEmpty

  def add(appendable: M[CQLQuery]): QueryCollection[M] = {
    val builder = cbf(queries)
    for (q <- appendable) builder += q
    new QueryCollection(builder.result())
  }

  def ++(appendable: M[CQLQuery]): QueryCollection[M] = add(appendable)

  def ++(st: QueryCollection[M]): QueryCollection[M] = add(st.queries)
}
