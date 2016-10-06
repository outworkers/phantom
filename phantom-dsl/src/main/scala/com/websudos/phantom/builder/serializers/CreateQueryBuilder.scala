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
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.QueryBuilder.Utils
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.KeySpace

sealed trait CreateOptionsBuilder {
  protected[this] def quotedValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    if (qb.nonEmpty) {
      qb.append(CQLSyntax.comma)
        .forcePad.appendSingleQuote(option)
        .append(CQLSyntax.Symbols.colon)
        .forcePad.appendSingleQuote(value)
    } else {
      qb.appendSingleQuote(option)
        .append(CQLSyntax.Symbols.colon)
        .forcePad.appendSingleQuote(value)
    }
  }

  protected[this] def simpleValue(qb: CQLQuery, option: String, value: String): CQLQuery = {
    qb.append(CQLSyntax.comma)
      .forcePad.appendSingleQuote(option)
      .append(CQLSyntax.Symbols.colon)
      .forcePad.append(value)
  }
}

sealed trait CompactionQueryBuilder extends CreateOptionsBuilder {

  def enabled(qb: CQLQuery, flag: Boolean): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.enabled, flag.toString)
  }

  def min_sstable_size[T : Numeric](qb: CQLQuery, size: T): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.min_sstable_size, size.toString)
  }

  def sstable_size_in_mb[T : Numeric](qb: CQLQuery, size: T): CQLQuery = {
    quotedValue(qb, CQLSyntax.CompactionOptions.sstable_size_in_mb, size.toString)
  }

  def tombstone_compaction_interval(qb: CQLQuery, size: String): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.tombstone_compaction_interval, size)
  }

  def tombstone_threshold(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.tombstone_threshold, size.toString)
  }

  def unchecked_tombstone_compaction(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.unchecked_tombstone_compaction, size.toString)
  }

  def cold_reads_to_omit(qb: CQLQuery, size: Double): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.cold_reads_to_omit, size.toString)
  }

  def base_time_seconds(qb: CQLQuery, value: Long): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.base_time_seconds, value.toString)
  }

  def timestamp_resolution(qb: CQLQuery, value: Long): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.timestamp_resolution, value.toString)
  }

  def max_sstable_age_days(qb: CQLQuery, value: Long): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.max_sstable_age_days, value.toString)
  }

  def max_threshold(qb: CQLQuery, value: Int): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.max_threshold, value.toString)
  }

  def min_threshold(qb: CQLQuery, value: Int): CQLQuery = {
    simpleValue(qb, CQLSyntax.CompactionOptions.min_threshold, value.toString)
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

sealed trait CachingQueryBuilder extends CreateOptionsBuilder {
  def keys(qb: CQLQuery, value: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.Keys, value)
  }

  def rows(qb: CQLQuery, value: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.Rows, value)
  }

  def rowsPerPartition(qb: CQLQuery, value: String): CQLQuery = {
    quotedValue(qb, CQLSyntax.RowsPerPartition, value)
  }
}


private[builder] class CreateTableBuilder extends
  CompactionQueryBuilder
  with CompressionQueryBuilder {

  object Caching extends CachingQueryBuilder

  def read_repair_chance(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.read_repair_chance, st)
  }

  def dclocal_read_repair_chance(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.dclocal_read_repair_chance, st)
  }

  def default_time_to_live(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.default_time_to_live, st)
  }

  def gc_grace_seconds(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.gc_grace_seconds, st)
  }

  def populate_io_cache_on_flush(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.populate_io_cache_on_flush, st)
  }

  def bloom_filter_fp_chance(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.bloom_filter_fp_chance, st)
  }

  def replicate_on_write(st: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.replicate_on_write, st)
  }

  def compression(qb: CQLQuery) : CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.compression, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def compaction(qb: CQLQuery) : CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.compaction, qb).pad.appendIfAbsent(CQLSyntax.Symbols.`}`)
  }

  def comment(qb: String): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.comment, CQLQuery.empty.appendSingleQuote(qb))
  }

  def caching(qb: String, wrapped: Boolean): CQLQuery = {
    val settings = if (!wrapped) {
      CQLQuery.empty.appendSingleQuote(qb)
    } else {
      CQLQuery.empty.append(Utils.curlyWrap(qb))
    }

    Utils.tableOption(
      CQLSyntax.CreateOptions.caching,
      settings
    )
  }

  def caching(qb: CQLQuery): CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.caching, CQLQuery.empty.append(qb))
  }

  def `with`(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.With).pad.append(clause)
  }

  /**
    * Creates an index on the keys on any column except for a Map column which requires special handling.
    * By default, mixing an index in a column will result in an index created on the values of the column.
    *
    * @param table The name of the table to create the index on.
    * @param keySpace The keyspace to whom the table belongs to.
    * @param column The name of the column to create the secondary index on.
    * @return A CQLQuery containing the valid CQL of creating a secondary index on a Cassandra column.
    */
  def index(table: String, keySpace: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.index)
      .forcePad.append(CQLSyntax.ifNotExists)
      .forcePad.append(s"${table}_${column}_idx")
      .forcePad.append(CQLSyntax.On)
      .forcePad.append(QueryBuilder.keyspace(keySpace, table))
      .wrapn(column)
  }

  /**
   * Creates an index on the keys of a Map column.
   * By default, mixing an index in a column will result in an index created on the values of the map.
   * To allow secondary indexing on Keys, Cassandra appends a KEYS($column) wrapper to the CQL query.
   *
   * @param table The name of the table to create the index on.
   * @param keySpace The keyspace to whom the table belongs to.
   * @param column The name of the column to create the secondary index on.
   * @return A CQLQuery containing the valid CQL of creating a secondary index for the keys of a Map column.e
   */
  def mapIndex(table: String, keySpace: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.index)
      .forcePad.append(CQLSyntax.ifNotExists)
      .forcePad.append(s"${table}_${column}_idx")
      .forcePad.append(CQLSyntax.On)
      .forcePad.append(QueryBuilder.keyspace(keySpace, table))
      .wrapn(CQLQuery(CQLSyntax.Keys).wrapn(column))
  }

  /**
    * Creates an index on the entries of a Map column.
    * By default, mixing an index in a column will result in an index created on the values of the map.
    * To allow secondary indexing on entries, Cassandra appends a ENTRIES($column) wrapper to the CQL query.
    *
    * @param table The name of the table to create the index on.
    * @param keySpace The keyspace to whom the table belongs to.
    * @param column The name of the column to create the secondary index on.
    * @return A CQLQuery containing the valid CQL of creating a secondary index for the entries of a Map column.
    */
  def mapEntries(table: String, keySpace: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.index)
      .forcePad.append(CQLSyntax.ifNotExists)
      .forcePad.append(s"${table}_${column}_idx")
      .forcePad.append(CQLSyntax.On)
      .forcePad.append(QueryBuilder.keyspace(keySpace, table))
      .wrapn(CQLQuery(CQLSyntax.Entries).wrapn(column))
  }

  def clusteringOrder(orderings: List[(String, String)]): CQLQuery = {
    val list = orderings.foldRight(List.empty[String]){ case ((key, value), l) =>
      (key + " " + value) :: l
    }

    CQLQuery(CQLSyntax.CreateOptions.clustering_order).wrap(list)
  }

  def defaultCreateQuery(
    keyspace: String,
    table: String,
    tableKey: String,
    columns: Seq[CQLQuery]
  ): CQLQuery = {
    CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.table)
      .forcePad.append(QueryBuilder.keyspace(keyspace, table)).forcePad
      .append(CQLSyntax.Symbols.`(`)
      .append(QueryBuilder.Utils.join(columns: _*))
      .append(CQLSyntax.Symbols.`,`)
      .forcePad.append(tableKey)
      .append(CQLSyntax.Symbols.`)`)
  }

  def createIfNotExists(
    keyspace: String,
    table: String,
    tableKey: String,
    columns: Seq[CQLQuery]
  ): CQLQuery = {
      CQLQuery(CQLSyntax.create).forcePad.append(CQLSyntax.table)
        .forcePad.append(CQLSyntax.ifNotExists)
        .forcePad.append(QueryBuilder.keyspace(keyspace, table))
        .forcePad.append(CQLSyntax.Symbols.`(`)
        .append(QueryBuilder.Utils.join(columns: _*))
        .append(CQLSyntax.Symbols.`,`)
        .forcePad.append(tableKey)
        .append(CQLSyntax.Symbols.`)`)
  }


}
