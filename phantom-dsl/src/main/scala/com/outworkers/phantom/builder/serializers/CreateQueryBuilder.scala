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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
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

private[builder] class CreateTableBuilder {

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
    Utils.tableOption(CQLSyntax.CreateOptions.compression, qb)
  }

  def compaction(qb: CQLQuery) : CQLQuery = {
    Utils.tableOption(CQLSyntax.CreateOptions.compaction, qb)
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
