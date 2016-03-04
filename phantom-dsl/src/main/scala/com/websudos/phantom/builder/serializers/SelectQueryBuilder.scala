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
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed class OrderingModifier {

  def ascending(column: String): CQLQuery = {

    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.asc)
  }

  def descending(column: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.desc)
  }

  def orderBy(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.OrderBy).forcePad.append(clause)
  }

  /**
   * OrderBy method used in combination with an OrderQueryPart to provide final serialization
   * of a varargs call. It will take a list of queries in the "col ordering" format and produce a full clause.
   *
   * Example:
   *
   * {{{
   *   val clauses = Seq("name ASC", "id DESC", "datetime ASC")
   *   val output = orderBy(clauses: _*)
   *
   *   output = "ORDER BY (name ASC, id DESC, datetime ASC)"
   * }}}
   *
   * @param clauses A sequence of ordering clauses to include in the query.
   * @return A final ORDER BY clause, with all the relevant query parts appended.
   */
  def orderBy(clauses: CQLQuery*): CQLQuery = {

    clauses.size match {
      case 1 => CQLQuery(CQLSyntax.Selection.OrderBy).forcePad.append(clauses.head.queryString)
      case _ => CQLQuery(CQLSyntax.Selection.OrderBy).forcePad.wrap(clauses.map(_.queryString))
    }
  }

  def orderBy(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.Selection.OrderBy).forcePad.append(clause)
  }
}

private[builder] class SelectQueryBuilder {

  case object Ordering extends OrderingModifier

  /**
   * Creates a select all query from a table name and a keyspace.
   * Will return a query in the following format:
   *
   * {{{
   *   SELECT * FROM $keyspace.$tableName
   * }}}
   *
   * @param tableName The name of the table.
   * @param keyspace The name of the keyspace.
   * @return A CQLQuery matching the described pattern.
   */
  def select(tableName: String, keyspace: String): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .pad.append("*").forcePad
      .append(CQLSyntax.from)
      .forcePad.append(QueryBuilder.keyspace(keyspace, tableName))
  }

  /**
   * Selects an arbitrary number of columns given a table name and a keyspace.
   * Will return a query in the following format:
   *
   * {{{
   *   SELECT ($name1, $name2, ..) FROM $keyspace.$tableName
   * }}}
   *
   * @param tableName The name of the table.
   * @param keyspace The name of the keyspace.
   * @param names The names of the columns to include in the select.
   * @return A CQLQuery matching the described pattern.
   */
  def select(tableName: String, keyspace: String, names: String*): CQLQuery = {

    val cols = if (names.nonEmpty) CQLQuery(names) else CQLQuery(CQLSyntax.Symbols.`*`)

    CQLQuery(CQLSyntax.select)
      .pad.append(cols)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(QueryBuilder.keyspace(keyspace, tableName))
  }

  /**
   * Creates a select count query builder from a table name, a keyspace, and a list of names.
   * The result of a count returns the number of matches, so the argument to count is fixed.
   * It can either be the ALL symbol(*) or 1, as per the CQL spec.
   *
   * Will return a query in the following format:
   *
   * {{{
   *   SELECT COUNT(*) FROM $keyspace.$tableName
   * }}}
   * @param tableName The name of the table.
   * @param keyspace The name of the keyspace.
   * @return
   */
  def count(tableName: String, keyspace: String): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .forcePad.append(CQLSyntax.count)
      .wrapn(CQLSyntax.Symbols.`*`)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(QueryBuilder.keyspace(keyspace, tableName))
  }

  /**
   * Creates a select distinct query builder from a table name, a keyspace, and a list of names.
   * Will return a query in the following format:
   *
   * {{{
   *   SELECT DISTINCT ($name1, $name2, ..) FROM $keyspace.$tableName
   * }}}
   * @param tableName The name of the table.
   * @param keyspace The name of the keyspace.
   * @param names The names of the columns to include in the select.
   * @return
   */
  def distinct(tableName: String, keyspace: String, names: String*): CQLQuery = {
    val cols = if (names.nonEmpty) CQLQuery(names) else CQLQuery(CQLSyntax.Symbols.`*`)

    CQLQuery(CQLSyntax.select)
      .forcePad.append(CQLSyntax.distinct)
      .forcePad.append(cols)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(QueryBuilder.keyspace(keyspace, tableName))
  }


  /**
   * Creates a select  query builder from a table name, a keyspace, and an arbitrary clause.
   * This is used to serialise SELECT functions, such as WRITETIME or other valid expressions.
   * Will return a query in the following format:
   *
   * {{{
   *   SELECT $clause FROM $keyspace.$tableName
   * }}}
   * @param tableName The name of the table.
   * @param keyspace The name of the keyspace.
   * @param clause The CQL clause to use as the select list value.
   * @return
   */
  def select(tableName: String, keyspace: String, clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.select)
      .pad.append(clause)
      .pad.append(CQLSyntax.from)
      .pad.append(QueryBuilder.keyspace(keyspace, tableName))
  }

  def allowFiltering(): CQLQuery = {
    CQLQuery(CQLSyntax.allowFiltering)
  }

  def allowFiltering(qb: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.allowFiltering)
  }

  /**
   * Creates a select clause chaining the "dateOf" operator.
   *
   * Example output:
   *
   * {{{
   *   dateOf(column)
   * }}}
   *
   * @param column The name of the column to apply the operation to.
   * @return A CQL query wrapping the "dateOf" clause and the column.
   */
  def dateOf(column: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.DateOf).wrapn(column)
  }

  def unixTimestampOf(column: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.UnixTimestampOf).wrapn(column)
  }

  def now(): CQLQuery = {
    CQLQuery("now()")
  }

  def writetime(col: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.Writetime).wrapn(col)
  }

  def maxTimeuuid(dateString: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.MaxTimeUUID).wrapn(dateString)
  }

  def minTimeuuid(dateString: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.MinTimeUUID).wrapn(dateString)
  }

  def blobAsText(qb: CQLQuery): CQLQuery = {
    blobAsText(qb.queryString)
  }

  def blobAsText(column: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.BlobAsText).wrapn(column)
  }


}
