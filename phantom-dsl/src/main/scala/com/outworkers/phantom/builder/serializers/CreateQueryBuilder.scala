/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.KeySpace

import scala.util.Try

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
    if (Try(value.toInt).isSuccess) {
      simpleValue(qb, CQLSyntax.RowsPerPartition, value.toString)
    } else {
      quotedValue(qb, CQLSyntax.RowsPerPartition, value)
    }
  }

  def rowsPerPartition(qb: CQLQuery, value: Int): CQLQuery = {
    simpleValue(qb, CQLSyntax.RowsPerPartition, value.toString)
  }
}

private[builder] class CreateTableBuilder {

  object Caching extends CachingQueryBuilder

  def partitionKey(keys: List[String]): CQLQuery = {
    keys match {
      case Nil => CQLQuery.empty
      case head :: Nil => CQLQuery(head)
      case _ :: _ => CQLQuery.empty.wrapn(keys)
    }
  }

  /**
    * This method will filter the columns from a Clustering Order definition.
    * It is used to define TimeSeries tables, using the ClusteringOrder trait
    * combined with a directional trait, either Ascending or Descending.
    *
    * This method will simply add to the trailing of a query.
    * @return The clustering key, defined as a string or the empty string.
    */
  def clusteringKey(keys: List[String]): CQLQuery = {
    keys match {
      case _ :: _ => CQLQuery.empty.pad.append("WITH CLUSTERING ORDER BY").wrap(keys)
      case Nil => CQLQuery.empty
    }
  }

  /**
    * This method will define the PRIMARY_KEY of the table.
    * <ul>
    *   <li>
    *    For more than one partition key, it will define a Composite Key.
    *    Example: PRIMARY_KEY((partition_key_1, partition_key2), primary_key_1, etc..)
    *   </li>
    *   <li>
    *     For a single partition key, it will define a Compound Key.
    *     Example: PRIMARY_KEY(partition_key_1, primary_key_1, primary_key_2)
    *   </li>
    *   <li>
    *     For no partition key, it will throw an exception.
    *   </li>
    * </ul>
    * @return A string value representing the primary key of the table.
    */
  def primaryKey(
    partitions: List[String],
    primaries: List[String] = Nil,
    clusteringKeys: List[String] = Nil
  ): CQLQuery = {
    val root = CQLQuery("PRIMARY KEY").forcePad
      .append(CQLSyntax.`(`)
      .append(partitionKey(partitions))

    val stage2 = if (primaries.nonEmpty) {
      // This only works because the macro prevents the user from defining both primaries and clustering keys
      // in the same table.
      val finalKeys = primaries.distinct
      root.append(CQLSyntax.comma).forcePad
        .append(finalKeys)
        .append(CQLSyntax.`)`)
    } else {
      root.append(CQLSyntax.`)`)
    }

    clusteringKeys match {
      case head :: tail => stage2.append(clusteringKey(clusteringKeys))
      case _ => stage2
    }
  }

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
    if (clause.nonEmpty) {
      CQLQuery(CQLSyntax.With).forcePad.append(clause)
    } else {
      CQLQuery.empty
    }
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
    val list = orderings.foldRight(List.empty[String]) { case ((key, value), l) =>
      (key + " " + value) :: l
    }

    CQLQuery(CQLSyntax.CreateOptions.clustering_order).wrap(list)
  }

  def sasiIndexName(tableName: String, columnName: String): CQLQuery = {
    CQLQuery(tableName)
      .append(CQLSyntax.Symbols.underscsore)
      .append(columnName)
      .append(CQLSyntax.Symbols.underscsore)
      .append(CQLSyntax.SASI.suffix)
  }

  def createSASIIndex(
    keySpace: KeySpace,
    tableName: String,
    indexName: CQLQuery,
    columnName: String,
    options: CQLQuery
  ): CQLQuery = {
    CQLQuery(CQLSyntax.create)
      .forcePad.append(CQLSyntax.custom)
      .forcePad.append(CQLSyntax.index)
      .forcePad.append(CQLSyntax.ifNotExists)
      .forcePad.append(indexName)
      .forcePad.append(CQLSyntax.On)
      .forcePad.append(keySpace.name)
      .append(CQLSyntax.Symbols.dot)
      .append(tableName)
      .wrapn(columnName)
      .forcePad.append(CQLSyntax.using)
      .forcePad.append(CQLQuery.escape(CQLSyntax.SASI.indexClass))
      .forcePad.append(`with`(options))
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
