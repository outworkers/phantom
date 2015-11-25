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

import com.datastax.driver.core.{Statement, ResultSet, ConsistencyLevel, Session}
import com.twitter.util.{ Future => TwitterFuture }
import com.websudos.phantom.builder.query._
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime
import shapeless.ops.hlist.Reverse
import shapeless.{Generic, HList}

import scala.collection.JavaConverters._
import scala.concurrent.{Future => ScalaFuture, blocking}

private[phantom] trait PrepareMark {

  def symbol: String = "?"

  def qb: CQLQuery = CQLQuery("?")
}

object ? extends PrepareMark


class ExecutablePreparedQuery(val statement: Statement) extends ExecutableStatement {
  override val qb = CQLQuery.empty

  override def consistencyLevel: Option[ConsistencyLevel] = None

  override def future()(implicit session: Session, keySpace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(statement)
  }

  override def execute()(implicit session: Session, keySpace: KeySpace): TwitterFuture[ResultSet] = {
    twitterQueryStringExecuteToFuture(statement)
  }
}


class PreparedBlock[PS <: HList](qb: CQLQuery)(implicit session: Session, keySpace: KeySpace) {

  val query = blocking {
    session.prepare(qb.queryString)
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
    //noinspection ComparingUnrelatedTypes
    parameters map {
      case x if x.isInstanceOf[Some[_]] => x.asInstanceOf[Some[Any]].get
      case x if x.isInstanceOf[List[_]] => x.asInstanceOf[List[Any]].asJava
      case x if x.isInstanceOf[Set[_]] => x.asInstanceOf[Set[Any]].asJava
      case x if x.isInstanceOf[Map[_, _]] => x.asInstanceOf[Map[Any, Any]].asJava
      case x if x.isInstanceOf[DateTime] => x.asInstanceOf[DateTime].toDate
      case x if x.isInstanceOf[Enumeration#Value] => x.asInstanceOf[Enumeration#Value].toString
      case x => x
    } map(_.asInstanceOf[AnyRef])
  }


  def bind[V1 <: Product, VL1 <: HList, Reversed <: HList](v1: V1)(
    implicit rev: Reverse.Aux[PS, Reversed],
    gen: Generic.Aux[V1, VL1],
    ev: VL1 =:= Reversed
  ): ExecutablePreparedQuery = {
    val params = flattenOpt(v1.productIterator.toSeq)
    new ExecutablePreparedQuery(query.bind(params: _*))
  }
}