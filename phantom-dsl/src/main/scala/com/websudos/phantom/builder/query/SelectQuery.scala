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
package com.websudos.phantom.builder.query


import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContext, Future => ScalaFuture }

import com.datastax.driver.core.{Row, Session}
import com.twitter.util.{ Future => TwitterFuture}

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._

import com.websudos.phantom.connectors.KeySpace

import scala.util.Try

class SelectQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound,
  Chain <: WhereBound
](table: Table, qb: CQLQuery, rowFunc: Row => Record) extends Query[Table, Record, Limit, Order, Status, Chain](table, qb, rowFunc) with ExecutableQuery[Table,
  Record, Limit] {

  def fromRow(row: Row): Record = rowFunc(row)

  override protected[this] type QueryType[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ] = SelectQuery[T, R, L, O, S, C]

  protected[this] def create[
    T <: CassandraTable[T, _],
    R,
    L <: LimitBound,
    O <: OrderBound,
    S <: ConsistencyBound,
    C <: WhereBound
  ](t: T, q: CQLQuery, r: Row => R): QueryType[T, R, L, O, S, C] = {
    new SelectQuery[T, R, L, O, S, C](t, q, r)
  }

  //final def orderBy(clause: Table => Order)

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def one()(implicit session: Session, ctx: ExecutionContext, keySpace: KeySpace, ev: Limit =:= Unlimited): ScalaFuture[Option[Record]] = {
    new SelectQuery[Table, Record, Limited, Order, Status, Chain](table, QueryBuilder.limit(qb, 1), rowFunc).singleFetch()
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This will always use a LIMIT 1 in the Cassandra query.
   * @param session The Cassandra session in use.
   * @return
   */
  @implicitNotFound("You have already defined limit on this Query. You cannot specify multiple limits on the same builder.")
  def get()(implicit session: Session, keySpace: KeySpace, ev: Limit =:= Unlimited): TwitterFuture[Option[Record]] = {
    new SelectQuery[Table, Record, Limited, Order, Status, Chain](table, QueryBuilder.limit(qb, 1), rowFunc).singleCollect()
  }
}

private[phantom] class RootSelectBlock[T <: CassandraTable[T, _], R](table: T, rowFunc: Row => R, columns: List[String]) {
  private[phantom] def all: SelectQuery.Default[T, R] = new SelectQuery(table, QueryBuilder.select(table.tableName, columns: _*), rowFunc)

  def distinct: SelectQuery.Default[T, R] = new SelectQuery(table, QueryBuilder.distinct(table.tableName, columns: _*), rowFunc)

  private[this] def extractCount(r: Row): Long = {
    Try { r.getLong("count") }.toOption.getOrElse(0L)
  }

  def count: SelectQuery.Default[T, Long] = new SelectQuery(table, QueryBuilder.count(table.tableName, columns: _*), extractCount)
}

object RootSelectBlock {

  def apply[T <: CassandraTable[T, _], R](table: T, columns: List[String], row: Row => R): RootSelectBlock[T, R] = {
    new RootSelectBlock(table, row, columns)
  }
}

object SelectQuery {

  type Default[T <: CassandraTable[T, _], R] = SelectQuery[T, R, Unlimited, Unordered, Unspecified, Unchainned]

  def apply[T <: CassandraTable[T, _], R](table: T, qb: CQLQuery, row: Row => R): SelectQuery.Default[T, R] = {
    new SelectQuery(table, qb, row)
  }
}

private[phantom] trait SelectImplicits {
  final implicit def rootSelectBlockToSelectQuery[T <: CassandraTable[T, _], R](root: RootSelectBlock[T, R]): SelectQuery.Default[T, R] = {
    root.all
  }
}