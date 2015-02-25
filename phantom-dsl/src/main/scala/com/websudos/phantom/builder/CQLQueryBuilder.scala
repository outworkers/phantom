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



import com.datastax.driver.core.{ConsistencyLevel, Row}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.{ CQLQuery, ExecutableStatement }

sealed trait LimitBound
trait Limited extends LimitBound
trait Unlimited extends LimitBound

sealed trait OrderBound
trait Ordered extends OrderBound
trait Unordered extends OrderBound

sealed trait ConsistencyBound
trait Specified extends ConsistencyBound
trait Unspecified extends ConsistencyBound


trait CQLOperator {
  def name: String
}


trait CompactionQueryBuilder {
  def min_sstable_size(qb: CQLQuery, size: String): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.min_sstable_size)
      .forcePad.append(size)
  }

  def sstable_size_in_mb(qb: CQLQuery, size: String): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.sstable_size_in_mb)
      .forcePad.append(size)
  }

  def tombstone_compaction_interval(qb: CQLQuery, size: String): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.tombstone_compaction_interval)
      .forcePad.append(size)
  }

  def tombstone_threshold(qb: CQLQuery, size: Double): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.tombstone_threshold)
      .forcePad.append(size.toString)
  }

  def bucket_high(qb: CQLQuery, size: Double): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.bucket_high)
      .forcePad.append(size.toString)
  }

  def bucket_low(qb: CQLQuery, size: Double): CQLQuery = {
    qb.pad.append(CQLSyntax.CompactionOptions.bucket_low)
      .forcePad.append(size.toString)
  }
}


object QueryBuilder extends CompactionQueryBuilder {

  val syntax = CQLSyntax

  def join(qbs: CQLQuery*): CQLQuery = {
    CQLQuery(qbs.map(_.queryString).mkString(", "))
  }

  def escapeOptions(qb: CQLQuery): CQLQuery = {
    CQLQuery.empty.append(syntax.Symbols.`{`)
      .forcePad.append(qb)
      .pad.append(syntax.Symbols.`}`)
  }

  def using(qb: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.using)
  }

  def consistencyLevel(qb: CQLQuery, level: String): CQLQuery = {
    using(qb).pad.append(syntax.consistency).forcePad.append(level)
  }

  def `with`(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.`with`).append(clause)
  }

  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.and).append(clause)
  }

  def prependKeySpaceIfAbsent(keySpace: String, qb: CQLQuery): CQLQuery = {
    if (qb.queryString.startsWith(keySpace)) {
      qb
    }  else {
      qb.prepend(s"$keySpace.")
    }
  }

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

  def limit(qb: CQLQuery, value: Int): CQLQuery = {
    qb.pad.append(syntax.limit)
      .forcePad.append(value.toString)
  }


}


class Query[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, val qb: CQLQuery, row: Row => Record) extends ExecutableStatement {

  final def limit(limit: Int)(implicit ev: Limit =:= Unlimited): Query[Table, Record, Limited, Order, Status] = {
    new Query(table, QueryBuilder.limit(qb, limit), row)
  }

  final def consistencyLevel(level: ConsistencyLevel)(implicit ev: Status =:= Unspecified): Query[Table, Record, Limit, Order, Specified] = {
    new Query(table, QueryBuilder.consistencyLevel(qb, level.toString), row)
  }
}

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, qb: CQLQuery, row: Row => Record) extends Query[Table, Record, Limit, Order, Status](table, qb, row)

class UpdateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, qb: CQLQuery, row: Row => Record) extends Query[Table, Record, Limit, Order, Status](table, qb, row)

class DeleteQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Limit <: LimitBound,
  Order <: OrderBound,
  Status <: ConsistencyBound
](table: Table, qb: CQLQuery, row: Row => Record) extends Query[Table, Record, Limit, Order, Status](table, qb, row)




