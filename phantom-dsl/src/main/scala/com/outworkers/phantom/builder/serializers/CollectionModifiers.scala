/*
 * Copyright 2013-2017 Outworkers, Limited.
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
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

private[builder] abstract class CollectionModifiers(queryBuilder: QueryBuilder) extends BaseModifiers {

  def tupled(name: String, tuples: String*): CQLQuery = {
    CQLQuery(name).wrap(queryBuilder.Utils.join(tuples))
  }

  def tuple(name: String, tuples: String*): CQLQuery = {
    CQLQuery(name).forcePad.append(CQLSyntax.Collections.tuple)
      .wrap(queryBuilder.Utils.join(tuples))
      .append(CQLSyntax.Symbols.`>`)
  }

  def frozen(column: String, definition: String): CQLQuery = {
    frozen(column, CQLQuery(definition))
  }

  /**
   * This will pre-fix and post-fix the given value with the "<>" diamond syntax.
   * It is used to define the collection type of a column.
   *
   * Sample outputs would be:
   * {{{
   *   dimond("list", "int") = list<int>
   *   dimond("set", "varchar") = set<varchar>
   * }}}
   *
   * @param collection The name of the collection in use.
   * @param value The value, usually the type of the CQL collection.
   * @return A CQL query serialising the CQL collection column definition syntax.
   */
  def diamond(collection: String, value: String): CQLQuery = {
    CQLQuery(collection)
      .append(CQLSyntax.Symbols.`<`)
      .append(value).
      append(CQLSyntax.Symbols.`>`)
  }

  def prepend(column: String, values: String*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        queryBuilder.Utils.collection(values).queryString,
        CQLSyntax.Symbols.plus,
        column
      )
    )
  }

  def prepend(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(valueDef, CQLSyntax.Symbols.plus, column)
    )
  }

  def append(column: String, values: String*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        column, CQLSyntax.Symbols.plus,
        queryBuilder.Utils.collection(values).queryString
      )
    )
  }

  def append(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(column, CQLSyntax.Symbols.plus, valueDef)
    )
  }

  def discard(column: String, values: String*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.-,
        queryBuilder.Utils.collection(values).queryString
      )
    )
  }

  def discard(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(column, CQLSyntax.Symbols.-, valueDef)
    )
  }

  def add(column: String, values: Set[String]): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.plus,
        queryBuilder.Utils.set(values)
      )
    )
  }

  /**
   * Creates a set removal query, to remove the given values from the name set column.
   * Assumes values are already serialised to their CQL form and escaped.
   *
   * {{{
   *  setColumn = setColumn - {`test`, `test2`}
   * }}}
   *
   * @param column The name of the set column.
   * @param values The set of values, pre-serialized and escaped.
   * @return A CQLQuery set remove query as described above.
   */
  def remove(column: String, values: Set[String]): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.-,
        queryBuilder.Utils.set(values)
      )
    )
  }

  def mapSet(column: String, key: String, value: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(key).append(CQLSyntax.Symbols.`]`)
      .forcePad.append(CQLSyntax.eqs)
      .forcePad.append(value)
  }

  def setIdX(column: String, index: String, value: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(index).append(CQLSyntax.Symbols.`]`)
      .forcePad.append(CQLSyntax.eqs)
      .forcePad.append(value)
  }

  def put(column: String, pairs: (String, String)*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.`=`).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.plus,
        queryBuilder.Utils.map(pairs)
      )
    )
  }

  def serialize(set: Set[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`)
      .forcePad.append(CQLQuery(set))
      .forcePad.append(CQLSyntax.Symbols.`}`)
  }

  def serialize(col: Map[String, String] ): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .append(CQLQuery(col.map { case (key, value) => s"$key : $value" }))
      .forcePad.append(CQLSyntax.Symbols.`}`)
  }

  def mapType(keyType: String, valueType: String): CQLQuery = {
    diamond(CQLSyntax.Collections.map, CQLQuery(List(keyType, valueType)).queryString)
  }

  def listType(valueType: String): CQLQuery = {
    diamond(CQLSyntax.Collections.list, valueType)
  }

  def setType(valueType: String): CQLQuery = {
    diamond(CQLSyntax.Collections.set, valueType)
  }

  def mapColumnType(column: String, key: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(key).append(CQLSyntax.Symbols.`]`)
  }

  def frozen(name: String, cassandraType: CQLQuery): CQLQuery = {
    CQLQuery(name).forcePad.append(
      diamond(CQLSyntax.Collections.frozen, cassandraType.queryString)
    )
  }
}
