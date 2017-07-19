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
package com.outworkers.phantom.builder.query.prepared

import com.datastax.driver.core.{QueryOptions => _, _}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, ExecutableStatement}
import com.outworkers.phantom.builder.{LimitBound, Unlimited}
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}
import com.outworkers.phantom.macros.BindHelper
import com.outworkers.phantom.{CassandraTable, ResultSet, Row}
import shapeless.ops.hlist.Tupler
import shapeless.{Generic, HList, HNil}

import scala.concurrent.{ExecutionContextExecutor, blocking, Future => ScalaFuture}

private[phantom] trait PrepareMark {

  def symbol: String = "?"

  def qb: CQLQuery = CQLQuery(symbol)
}

object PrepareMark {
  val ? = new PrepareMark {}
}

class ExecutablePreparedQuery(
  val statement: Statement,
  val options: QueryOptions
) extends ExecutableStatement with Batchable {
  override val qb: ExecutableCqlQuery = ExecutableCqlQuery.empty

  override def statement()(implicit session: Session): Statement = {
    statement.setConsistencyLevel(options.consistencyLevel.orNull)
  }
}

class ExecutablePreparedSelectQuery[
  Table <: CassandraTable[Table, _],
  R,
  Limit <: LimitBound
](val st: Statement, fn: Row => R, val options: QueryOptions)

abstract class PreparedFlattener(qb: CQLQuery)(
  implicit session: Session, keySpace: KeySpace
) extends SessionAugmenterImplicits {

  protected[this] val query: PreparedStatement = {
    blocking(session.prepare(qb.queryString))
  }
}

class PreparedBlock[PS <: HList](val qb: CQLQuery, val options: QueryOptions)
  (implicit session: Session, keySpace: KeySpace) extends PreparedFlattener(qb) {

  /**
    * Method used to bind a set of arguments to a prepared query in a typesafe manner.
    *
    * @param v1 The argument used for the assertion, inferred as a tuple by the compiler.
    * @param gen The Shapeless Tupler implicit builder to cast HList to a Tuple.
    * @param ev The equality parameter to check that the types provided in the tuple match the prepared query.
    * @tparam V1 The argument tuple type, auto-tupled by the compiler from varargs.
    * @tparam Out The type argument used to cast the HList to a Tuple.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V1 <: Product, Out <: HList](v1: V1)(
    implicit gen: Generic.Aux[V1, Out],
    binder: BindHelper[V1],
    ev: Out =:= PS
  ): ExecutablePreparedQuery = {
    val bb = binder.bind(
      query,
      v1,
      session.protocolVersion
    )

    new ExecutablePreparedQuery(bb, options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
    *
    * @param v A single argument that will be interpreted as a sequence of 1 for binding.
    * @tparam V The type of the argument.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V](v: V)(
    implicit ev: Primitive[V],
    binder: BindHelper[V]
  ): ExecutablePreparedQuery = {
    new ExecutablePreparedQuery(
      binder.bind(query, v, session.protocolVersion),
      options
    )
  }
}

class PreparedSelectBlock[
  T <: CassandraTable[T, _],
  R,
  Limit <: LimitBound,
  PS <: HList
  ](qb: CQLQuery, fn: Row => R, options: QueryOptions)
(implicit session: Session, keySpace: KeySpace) extends PreparedFlattener(qb) {

  /**
    * Method used to bind a set of arguments to a prepared query in a typesafe manner.
    *
    * @param v1 The argument used for the assertion, inferred as a tuple by the compiler.
    * @param tp The Shapeless Tupler implicit builder to cast HList to a Tuple.
    * @param ev The equality parameter to check that the types provided in the tuple match the prepared query.
    * @tparam V1 The argument tuple type, auto-tupled by the compiler from varargs.
    * @tparam Out The type argument used to cast the HList to a Tuple.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V1 <: Product, Out <: Product](v1: V1)(
    implicit tp: Tupler.Aux[PS, Out],
    binder: BindHelper[V1],
    ev: V1 =:= Out
  ): ExecutablePreparedSelectQuery[T, R, Limit] = {
    val bb = binder.bind(
      query,
      v1,
      session.protocolVersion
    )

    new ExecutablePreparedSelectQuery(bb, fn, options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
    *
    * @param v A single argument that will be interpreted as a sequence of 1 for binding.
    * @tparam V The type of the argument.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V](v: V)(
    implicit ev: Primitive[V],
    binder: BindHelper[V]
  ): ExecutablePreparedSelectQuery[T, R, Limit] = {
    new ExecutablePreparedSelectQuery(
      binder.bind(query, v, session.protocolVersion),
      fn,
      options
    )
  }
}