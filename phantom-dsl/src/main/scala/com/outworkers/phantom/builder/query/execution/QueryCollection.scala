/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

import scala.collection.compat._

class QueryCollection[M[X] <: IterableOnce[X]](val queries: M[ExecutableCqlQuery])(
  implicit cbf: BuildFrom[M[ExecutableCqlQuery], ExecutableCqlQuery, M[ExecutableCqlQuery]]
) {

  def isEmpty: Boolean = queries.iterator.isEmpty

  def size: Int = queries.iterator.size

  def appendAll(appendable: M[ExecutableCqlQuery]): QueryCollection[M] = {
    val builder = cbf.newBuilder(queries)

    for (q <- queries.iterator) builder += q
    for (q <- appendable.iterator) builder += q
    new QueryCollection(builder.result())
  }
}
