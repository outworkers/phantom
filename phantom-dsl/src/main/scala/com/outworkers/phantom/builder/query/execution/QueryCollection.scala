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

import scala.collection.generic.CanBuildFrom

class QueryCollection[M[X] <: TraversableOnce[X]](val queries: M[ExecutableCqlQuery])(
  implicit cbf: CanBuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
) {

  def isEmpty: Boolean = queries.isEmpty

  def size: Int = queries.size

  def appendAll(appendable: M[ExecutableCqlQuery]): QueryCollection[M] = {
    val builder = cbf(queries)

    for (q <- queries) builder += q
    for (q <- appendable) builder += q
    new QueryCollection(builder.result())
  }

  def ++(st: QueryCollection[M]): QueryCollection[M] = appendAll(st.queries)
}
