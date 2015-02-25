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
