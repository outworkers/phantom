package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

private[phantom] trait AlterQueryBuilder {

  /**
   * Creates the ADD part of an alter query for a column name and a type.
   * This is used when new columns are added to an existing definitions.
   * It will return only the last part of the clause, as follows:
   *
   * {{{
   *  ADD $column $columnType.
   * }}}
   *
   * @param qb The existing built query to append to.
   * @param column The name of the column to add in the alter query.
   * @param columnType The type of the new column.
   * @return A CQLQuery enclosing the ADD part of an alter query.
   */
  def add(qb: CQLQuery, column: String, columnType: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Add)
      .forcePad.append(column)
      .forcePad.append(columnType)
  }

  /**
   * Used to create the query string of a static column.
   * Creates the ADD part of an alter query for a column name and a type.
   * Appends a STATIC modifier to the column definition.
   * This is used when new columns are added to an existing definitions.
   * It will return only the last part of the clause, as follows:
   *
   * {{{
   *  ADD $column $columnType.
   * }}}
   *
   * @param qb The existing built query to append to.
   * @param column The name of the column to add in the alter query.
   * @param columnType The type of the new column.
   * @return A CQLQuery enclosing the ADD part of an alter query.
   */
  def addStatic(qb: CQLQuery, column: String, columnType: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Add)
      .forcePad.append(column)
      .forcePad.append(columnType)
      .forcePad.append(CQLSyntax.static)
  }

  def add(qb: CQLQuery, definition: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Add)
      .forcePad.append(definition)
  }

  /**
   * Creates the alter part of an ALTER query, used when changing the type of existing columns.
   *
   * {{{
   *   ALTER $column TYPE $columnType.
   * }}}
   *
   * @param column The name of the column to add in the alter query.
   * @param columnType The type of the new column.
   * @return A CQLQuery enclosing the ALTER part of an alter query.
   */
  def alter(qb: CQLQuery, column: String, columnType: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Alter)
      .forcePad.append(column)
      .forcePad.append(CQLSyntax.`type`)
      .forcePad.append(columnType)
  }

  def rename(qb: CQLQuery, column: String, newColumn: String) = {
    qb.pad.append(CQLSyntax.Alter.Rename)
      .forcePad.append(column)
      .forcePad.append(newColumn)
  }

  def drop(qb: CQLQuery, column: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Drop)
      .forcePad.append(column)
  }
}