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
package com.websudos.phantom.builder

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.query.ExecutableQuery

sealed trait LimitBound
trait Limited extends LimitBound
trait Unlimited extends LimitBound

sealed trait OrderBound
trait Ordered extends OrderBound
trait Unordered extends OrderBound


trait CQLOperator {
  def name: String
}




object QueryBuilder {

  val syntax = CQLSyntax

  def where(query: CQLQuery, op: CQLOperator, name: String, value: String): CQLQuery = {
    query.pad.append(syntax.where)
      .pad.append(name)
      .pad.append(op.name)
      .forcePad.append(value)
  }

  def where(query: CQLQuery, condition: CQLQuery): CQLQuery = {
    query.pad.append(syntax.where).pad.append(condition)
  }

  def select(tableName: String): CQLQuery = {
    CQLQuery(syntax.select)
      .forcePad.append("*").forcePad
      .append(syntax.from)
      .forcePad.appendEscape(tableName)
  }

  def select(tableName: String, names: String*): CQLQuery = {
    CQLQuery(syntax.select)
      .pad.append(names)
      .forcePad.append(syntax.from)
      .forcePad.appendEscape(tableName)
  }

  def select(tableName: String, clause: CQLQuery) = {
    CQLQuery(syntax.select)
      .pad.append(clause)
      .pad.append(syntax.from)
      .pad.appendEscape(tableName)
  }

  def limit(qb: CQLQuery, value: String): CQLQuery = {
    qb.pad.append(syntax.limit)
      .forcePad.append(value)
  }


}

class Query[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound
](table: Table, qb: CQLQuery, row: Row => Record) extends ExecutableQuery[Table, Record] with CQLQuery {

  final def limit(limit: Int)(implicit ev: Limit =:= Unlimited): Query[Table, Record, Unlimited, Order] = {
    new Query(table, QueryBuilder.limit(qb, limit.toString), row)
  }
}
