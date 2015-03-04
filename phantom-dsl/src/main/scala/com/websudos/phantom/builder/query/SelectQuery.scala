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


import scala.concurrent.{ExecutionContext, Future => ScalaFuture }

import com.datastax.driver.core.{Row, Session}
import com.twitter.util.{ Future => TwitterFuture}

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.connectors.KeySpace

class SelectQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, qb: CQLQuery, rowFunc: Row => Record) extends Query[Table, Record, Limit, Order, Status](table, qb, rowFunc) with ExecutableQuery[Table,
  Record, Limit] {

  def fromRow(row: Row): Record = rowFunc(row)

  /**
   * Returns the first row from the select ignoring everything else
   * @param session The Cassandra session in use.
   * @param ctx The Execution Context.
   * @return
   */
  def one()(implicit session: Session, ctx: ExecutionContext, keySpace: KeySpace, ev: Limit =:= Unlimited): ScalaFuture[Option[Record]] = {
    new SelectQuery[Table, Record, Limited, Order, Status](table, QueryBuilder.limit(qb, 1), rowFunc).singleFetch()
  }

  /**
   * Returns the first row from the select ignoring everything else
   * This will always use a LIMIT 1 in the Cassandra query.
   * @param session The Cassandra session in use.
   * @return
   */
  def get()(implicit session: Session, keySpace: KeySpace, ev: Limit =:= Unlimited): TwitterFuture[Option[Record]] = {
    new SelectQuery[Table, Record, Limited, Order, Status](table, QueryBuilder.limit(qb, 1), rowFunc).singleCollect()
  }
}
