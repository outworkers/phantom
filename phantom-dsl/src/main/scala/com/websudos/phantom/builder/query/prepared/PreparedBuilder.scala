/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query.prepared

import com.datastax.driver.core.{QueryOptions => _, _}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query._
import com.websudos.phantom.builder.{LimitBound, Unlimited}
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime
import shapeless.{Generic, HList}
import shapeless.ops.hlist.Tupler

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, blocking, Future => ScalaFuture}

private[phantom] trait PrepareMark {

  def symbol: String = "?"

  def qb: CQLQuery = CQLQuery("?")
}

class ExecutablePreparedQuery(val statement: Statement, val options: QueryOptions) extends ExecutableStatement with Batchable {
  override val qb = CQLQuery.empty

  override def statement()(implicit session: Session): Statement = {
    statement
      .setConsistencyLevel(options.consistencyLevel.orNull)
  }
}

class ExecutablePreparedSelectQuery[
Table <: CassandraTable[Table, _],
R,
Limit <: LimitBound
](val st: Statement, fn: Row => R, val options: QueryOptions) extends ExecutableQuery[Table, R, Limit] {

  override def fromRow(r: Row): R = fn(r)

  override def future()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(st)
  }


  /**
    * Returns the first row from the select ignoring everything else
    * @param session The implicit session provided by a [[com.websudos.phantom.connectors.Connector]].
    * @param keySpace The implicit keySpace definition provided by a [[com.websudos.phantom.connectors.Connector]].
    * @param ev The implicit limit for the query.
    * @param ec The implicit Scala execution context.
    * @return A Scala future guaranteed to contain a single result wrapped as an Option.
    */
  override def one()(
    implicit session: Session,
    keySpace: KeySpace,
    ev: =:=[Limit, Unlimited],
    ec: ExecutionContextExecutor
  ): ScalaFuture[Option[R]] = {
    singleFetch()
  }

  override def qb: CQLQuery = CQLQuery.empty
}

abstract class PreparedFlattener(qb: CQLQuery)(implicit session: Session, keySpace: KeySpace) {

  protected[this] val query: PreparedStatement = {
    blocking(session.prepare(qb.queryString))
  }

  def flattenOpt(param: Any): Any = {
    //noinspection ComparingUnrelatedTypes
    param match {
      case x if x.isInstanceOf[Some[_]] => flattenOpt(x.asInstanceOf[Some[_]].get)
      case x if x.isInstanceOf[None.type] => None.orNull.asInstanceOf[Any]
      case x if x.isInstanceOf[List[_]] => x.asInstanceOf[List[Any]].asJava
      case x if x.isInstanceOf[Set[_]] => x.asInstanceOf[Set[Any]].asJava
      case x if x.isInstanceOf[Map[_, _]] => x.asInstanceOf[Map[Any, Any]].asJava
      case x if x.isInstanceOf[DateTime] => x.asInstanceOf[DateTime].toDate
      case x if x.isInstanceOf[Enumeration#Value] => x.asInstanceOf[Enumeration#Value].toString
      case x if x.isInstanceOf[BigDecimal] => x.asInstanceOf[BigDecimal].bigDecimal
      case x if x.isInstanceOf[BigInt] => x.asInstanceOf[BigInt].bigInteger
      case x => x
    }
  }

  /**
    * Cleans up the series of parameters passed to the bind query to match
    * the codec registry collection that the Java Driver has internally.
    *
    * If the type of the object passed through to the driver doesn't match a known type for the specific Cassandra column
    * type, then the driver will crash with an error.
    *
    * There are known associations of (Cassandra Column Type -> Java Type) that we need to provide for binding to work.
    *
    * @param parameters The sequence of parameters to bind.
    * @return A clansed set of parameters.
    */
  protected[this] def flattenOpt(parameters: Seq[Any]): Seq[AnyRef] = {
    parameters map flattenOpt map (_.asInstanceOf[AnyRef])
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
    ev: Out =:= PS
  ): ExecutablePreparedQuery = {
    val params = flattenOpt(v1.productIterator.toSeq)
    new ExecutablePreparedQuery(query.bind(params: _*), options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
    *
    * @param v A single argument that will be interpreted as a sequence of 1 for binding.
    * @tparam V The type of the argument.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V](v: V): ExecutablePreparedQuery = {
    val params = flattenOpt(Seq(v))
    new ExecutablePreparedQuery(query.bind(params: _*), options)
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
    ev: V1 =:= Out
  ): ExecutablePreparedSelectQuery[T, R, Limit] = {
    val params = flattenOpt(v1.productIterator.toSeq)
    new ExecutablePreparedSelectQuery(query.bind(params: _*), fn, options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
    *
    * @param v A single argument that will be interpreted as a sequence of 1 for binding.
    * @tparam V The type of the argument.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V](v: V): ExecutablePreparedSelectQuery[T, R, Limit] = {
    val params = flattenOpt(Seq(v))
    new ExecutablePreparedSelectQuery(query.bind(params: _*), fn, options)
  }

}