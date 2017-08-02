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
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

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
      .forcePad.append(CQLSyntax.Type)
      .forcePad.append(columnType)
  }


  /**
   * Adds an option to an ALTER query from the list of CREATE query options.
   * Inside an ALTER query, the `with` definition is a chainned query, not a multi-part query.
   * That means every single option call will alter the init part of a query with no
   * preservation of state or immutable state.
   *
   * This specific type of option refers to things like:
   * - Compaction strategies
   * - Compression strategies
   * - etc..
   *
   * @param qb The init query clause.
   * @param clause The clause or option to append to the root query.
   * @return A new CQL query, where the underlying query contains an option clause.
   */
  def option(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.pad.append(CQLSyntax.With).pad.append(clause)
  }

  def rename(qb: CQLQuery, column: String, newColumn: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Rename)
      .forcePad.append(column)
      .forcePad.append(CQLSyntax.To)
      .forcePad.append(newColumn)
  }

  def drop(qb: CQLQuery, column: String): CQLQuery = {
    qb.pad.append(CQLSyntax.Alter.Drop)
      .forcePad.append(column)
  }


  def dropTable(table: String, keyspace: String): CQLQuery = {
    CQLQuery(CQLSyntax.Alter.Drop)
      .forcePad.append(CQLSyntax.table)
      .forcePad.append(QueryBuilder.keyspace(keyspace, table))
  }

  def dropTableIfExist(table: String, keyspace: String): CQLQuery = {
    CQLQuery(CQLSyntax.Alter.Drop)
      .forcePad.append(CQLSyntax.table)
      .forcePad.append(CQLSyntax.ifExists)
      .forcePad.append(QueryBuilder.keyspace(keyspace, table))
  }

  def alter(tableName: String): CQLQuery = {
    CQLQuery(CQLSyntax.alter)
      .forcePad.append(CQLSyntax.table)
      .forcePad.append(tableName)
  }

}