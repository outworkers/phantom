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
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

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


private[builder] class CreateTableBuilder extends CompactionQueryBuilder with CompressionQueryBuilder {

  private[this] def tableOption(option: String, value: String): CQLQuery = {
    Utils.concat(option, CQLSyntax.Symbols.`=`, value)
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

  def default_time_to_live(st: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.default_time_to_live, st)
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
    tableOption(CQLSyntax.CreateOptions.comment, CQLQuery.empty.appendSingleQuote(qb))
  }

  def caching(qb: String): CQLQuery = {
    tableOption(CQLSyntax.CreateOptions.caching, CQLQuery.empty.appendSingleQuote(qb))
  }

  def `with`(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.`with`).pad.append(clause)
  }

  def index(table: String, keySpace: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.index)
      .forcePad.append(CQLSyntax.ifNotExists)
      .forcePad.append(CQLSyntax.On)
      .forcePad.append(QueryBuilder.keyspace(keySpace, table))
      .wrap(column)
  }

}
