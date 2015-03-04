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


sealed trait CreateOptionsBuilder {
  protected[this] def quotedValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    qb.append(CQLSyntax.comma)
      .forcePad.appendSingleQuote(option)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.appendSingleQuote(value)
  }

  protected[this] def simpleValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    qb.append(CQLSyntax.comma)
      .forcePad.appendSingleQuote(option)
      .forcePad.append(CQLSyntax.Symbols.`:`)
      .forcePad.append(value)
  }
}

sealed trait CompactionQueryBuilder extends CreateOptionsBuilder {

  def min_sstable_size(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.min_sstable_size, size)
  }

  def sstable_size_in_mb(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.sstable_size_in_mb, size)
  }

  def tombstone_compaction_interval(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.tombstone_compaction_interval, size)
  }

  def tombstone_threshold(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.tombstone_threshold, size.toString)
  }

  def bucket_high(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.bucket_high, size.toString)
  }

  def bucket_low(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.bucket_low, size.toString)
  }
}

sealed trait CompressionQueryBuilder extends CreateOptionsBuilder {

  def chunk_length_kb(qb: CQLQuery, size: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompressionOptions.chunk_length_kb, size)
  }

  def crc_check_chance(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompressionOptions.crc_check_chance, size.toString)
  }
}


sealed trait CreateTableBuilder extends CompactionQueryBuilder with CompressionQueryBuilder {

  private[this] def tableOption(option: String, value: String): CQLQuery = {
    CQLQuery(option)
      .forcePad.append(CQLSyntax.Symbols.`=`)
      .forcePad.append(value)
  }

  private[this] def tableOption(option: String, value: CQLQuery): CQLQuery = {
    tableOption(option, value.queryString)
  }

  def read_repair_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.read_repair_chance, st)
  }

  def dclocal_read_repair_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.dclocal_read_repair_chance, st)
  }

  def gc_grace_seconds(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.gc_grace_seconds, st)
  }

  def populate_io_cache_on_flush(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.populate_io_cache_on_flush, st)
  }

  def bloom_filter_fp_chance(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.bloom_filter_fp_chance, st)
  }

  def replicate_on_write(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.replicate_on_write, st)
  }

  def compression(qb: CQLQuery) : CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.compression, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def compaction(qb: CQLQuery) : CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.compaction, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def comment(qb: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.replicate_on_write, CQLQuery.empty.appendSingleQuote(qb))
  }

  def caching(qb: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.caching, CQLQuery.empty.appendSingleQuote(qb))
  }
}


object QueryBuilder extends CompactionQueryBuilder with CompressionQueryBuilder {

  val syntax = CQLSyntax

  case object Create extends CreateTableBuilder

  def join(qbs: CQLQuery*): CQLQuery = {
    CQLQuery(qbs.map(_.queryString).mkString(", "))
  }

  def using(qb: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.using)
  }

  def consistencyLevel(qb: CQLQuery, level: String): CQLQuery = {
    using(qb).pad.append(syntax.consistency).forcePad.append(level)
  }

  def `with`(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.`with`).pad.append(clause)
  }

  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(syntax.and).forcePad.append(clause)
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




