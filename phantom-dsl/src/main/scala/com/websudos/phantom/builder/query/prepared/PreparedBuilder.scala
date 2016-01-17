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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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

import java.util.UUID

import com.datastax.driver.core.{ QueryOptions => _ , _ }
import com.twitter.util.{ Future => TwitterFuture }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.{LimitBound, Unlimited}
import com.websudos.phantom.builder.query._
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime
import shapeless.ops.hlist.Reverse
import shapeless.{Generic, HList}

import scala.collection.JavaConverters._
import scala.concurrent.{Future => ScalaFuture, ExecutionContext, blocking}

private[phantom] trait PrepareMark {

  def symbol: String = "?"

  def qb: CQLQuery = CQLQuery("?")
}

object ? extends PrepareMark

class ExecutablePreparedQuery(val statement: Statement, val options: QueryOptions) extends ExecutableStatement {
  override val qb = CQLQuery.empty

  override def future()(implicit session: Session, keySpace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(statement)
  }

  override def execute()(implicit session: Session, keySpace: KeySpace): TwitterFuture[ResultSet] = {
    twitterQueryStringExecuteToFuture(statement)
  }
}

class ExecutablePreparedSelectQuery[
  Table <: CassandraTable[Table, _],
  R,
  Limit <: LimitBound
](val st: Statement, fn: Row => R, val options: QueryOptions) extends ExecutableQuery[Table, R, Limit] {

  override def fromRow(r: Row): R = fn(r)

  override def future()(implicit session: Session, keySpace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(st)
  }

  override def execute()(implicit session: Session, keySpace: KeySpace): TwitterFuture[ResultSet] = {
    twitterQueryStringExecuteToFuture(st)
  }

  /**
    * Returns the first row from the select ignoring everything else
    * @param session The Cassandra session in use.
    * @return A Scala future guaranteed to contain a single result wrapped as an Option.
    */
  override def one()(implicit session: Session, ec: ExecutionContext, keySpace: KeySpace, ev: =:=[Limit, Unlimited]): ScalaFuture[Option[R]] = {
    singleFetch()
  }

  /**
    * Get the result of an operation as a Twitter Future.
    * @param session The Datastax Cassandra session.
    * @return A Twitter future wrapping the result.
    */
  override def get()(implicit session: Session, keySpace: KeySpace, ev: =:=[Limit, Unlimited]): TwitterFuture[Option[R]] = {
    singleCollect()
  }

  override def qb: CQLQuery = CQLQuery.empty
}

abstract class PreparedFlattener(qb: CQLQuery)(implicit session: Session, keySpace: KeySpace) {

  protected[this] val query: PreparedStatement = {
    blocking(session.prepare(qb.queryString))
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

    def flattenOpt(param: Any): Any = {
      //noinspection ComparingUnrelatedTypes
      param match {
        case x if x.isInstanceOf[UUID] => {
          Console.println("This is an instance of UUID")
          Console.println(x.asInstanceOf[UUID])
          x
        }
        case x if x.isInstanceOf[Some[_]] => flattenOpt(x.asInstanceOf[Some[_]].get)
        case x if x.isInstanceOf[None.type] => null.asInstanceOf[Any]
        case x if x.isInstanceOf[List[_]] => x.asInstanceOf[List[Any]].asJava
        case x if x.isInstanceOf[Set[_]] => x.asInstanceOf[Set[Any]].asJava
        case x if x.isInstanceOf[Map[_, _]] => x.asInstanceOf[Map[Any, Any]].asJava
        case x if x.isInstanceOf[DateTime] => x.asInstanceOf[DateTime].toDate
        case x if x.isInstanceOf[Enumeration#Value] => x.asInstanceOf[Enumeration#Value].toString
        case x if x.isInstanceOf[BigDecimal] => x.asInstanceOf[BigDecimal].bigDecimal
        case x => x
      }
    }

    parameters map flattenOpt map(_.asInstanceOf[AnyRef])
  }
}

class PreparedBlock[PS <: HList](val qb: CQLQuery, val options: QueryOptions)
  (implicit session: Session, keySpace: KeySpace) extends PreparedFlattener(qb) {

  /**
    * Method used to bind a set of arguments to a prepared query in a typesafe manner.
    * @param v1 The argument used for the assertion, inferred as a tuple by the compiler.
    * @param rev The Shapeless HList type reversal operation.
    * @param gen The Shapeless Generic implicit builder to cast the autotupled arguments to an HList.
    * @param ev The equality parameter to check that the types provided in the tuple match the prepared query.
    * @tparam V1 The argument tuple type, auto-tupled by the compiler from varargs.
    * @tparam VL1 The type argument used to cast the Generic.
    * @tparam Reversed The Type used to cast the inner HList type of the prepared query to the result of the reverse.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V1 <: Product, VL1 <: HList, Reversed <: HList](v1: V1)(
    implicit rev: Reverse.Aux[PS, Reversed],
    gen: Generic.Aux[V1, VL1],
    ev: VL1 =:= Reversed
  ): ExecutablePreparedQuery = {
    val params = flattenOpt(v1.productIterator.toSeq)
    new ExecutablePreparedQuery(query.bind(params: _*), options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
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
    * @param v1 The argument used for the assertion, inferred as a tuple by the compiler.
    * @param rev The Shapeless HList type reversal operation.
    * @param gen The Shapeless Generic implicit builder to cast the autotupled arguments to an HList.
    * @param ev The equality parameter to check that the types provided in the tuple match the prepared query.
    * @tparam V1 The argument tuple type, auto-tupled by the compiler from varargs.
    * @tparam VL1 The type argument used to cast the Generic.
    * @tparam Reversed The Type used to cast the inner HList type of the prepared query to the result of the reverse.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V1 <: Product, VL1 <: HList, Reversed <: HList](v1: V1)(
    implicit rev: Reverse.Aux[PS, Reversed],
    gen: Generic.Aux[V1, VL1],
    ev: VL1 =:= Reversed
  ): ExecutablePreparedSelectQuery[T, R, Limit] = {
    val params = flattenOpt(v1.productIterator.toSeq)
    new ExecutablePreparedSelectQuery(query.bind(params: _*), fn, options)
  }

  /**
    * Method used to bind a single argument to a prepared statement.
    * @param v A single argument that will be interpreted as a sequence of 1 for binding.
    * @tparam V The type of the argument.
    * @return An final form prepared select query that can be asynchronously executed.
    */
  def bind[V](v: V): ExecutablePreparedSelectQuery[T, R, Limit] = {
    val params = flattenOpt(Seq(v))
    new ExecutablePreparedSelectQuery(query.bind(params: _*), fn, options)
  }
}