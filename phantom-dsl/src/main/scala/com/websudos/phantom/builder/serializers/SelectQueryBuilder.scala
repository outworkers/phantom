package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed class OrderingModifier {

  def ascending(column: String): CQLQuery = {

    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.asc)
  }

  def descending(column: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Ordering.desc)
  }

  def orderBy(qb: CQLQuery, clause: CQLQuery) = {
    Utils.concat(qb, CQLSyntax.Selection.OrderBy, clause)
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
    CQLQuery(CQLSyntax.select)
      .forcePad.append(CQLSyntax.distinct)
      .pad.append(names)
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
  def select(tableName: String, keyspace: String, clause: CQLQuery) = {
    CQLQuery(CQLSyntax.select)
      .pad.append(clause)
      .pad.append(CQLSyntax.from)
      .pad.append(QueryBuilder.keyspace(keyspace, tableName))
  }

  def allowFiltering(qb: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.allowFiltering)
  }

  def dateOf(column: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.DateOf).wrapn(column)
  }

  def blobAsText(qb: CQLQuery): CQLQuery = {
    blobAsText(qb.queryString)
  }

  def blobAsText(column: String): CQLQuery = {
    CQLQuery(CQLSyntax.Selection.BlobAsText).wrapn(column)
  }


}
