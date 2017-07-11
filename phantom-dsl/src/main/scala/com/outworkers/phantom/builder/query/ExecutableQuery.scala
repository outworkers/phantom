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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{ResultSet => _, Row => _}
import com.outworkers.phantom.builder.LimitBound
import com.outworkers.phantom.builder.query.execution.ResultQueryInterface
import com.outworkers.phantom.{CassandraTable, Row}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}


private[this] object SequentialFutures {
  def sequencedTraverse[
    A,
    B,
    M[X] <: TraversableOnce[X]
  ](in: M[A])(fn: A => ScalaFuture[B])(implicit
    executor: ExecutionContextExecutor,
    cbf: CanBuildFrom[M[A], B, M[B]]
  ): ScalaFuture[M[B]] = {
    in.foldLeft(ScalaFuture.successful(cbf(in))) { (fr, a) =>
      for (r <- fr; b <- fn(a)) yield r += b
    }.map(_.result())
  }
}

/**
 * An ExecutableQuery implementation, meant to retrieve results from Cassandra.
 * This provides the root implementation of a Select query.
 *
 * @tparam T The class owning the table.
 * @tparam R The record type to store.
 */
trait ExecutableQuery[
  T <: CassandraTable[T, _],
  R,
  Limit <: LimitBound
] extends ResultQueryInterface[ScalaFuture, T, R, Limit] {

  def fromRow(r: Row): R

}
