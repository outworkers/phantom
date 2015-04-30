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


  def alter(tableName: String) = {
    CQLQuery(CQLSyntax.alter)
      .forcePad.append(CQLSyntax.table)
      .forcePad.append(tableName)
  }

}