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
import com.outworkers.phantom.builder.primitives.Primitive

private[builder] abstract class CollectionModifiers(queryBuilder: QueryBuilder) extends BaseModifiers {

  def tupled(tuples: String*): CQLQuery = {
    queryBuilder.Utils.join(tuples)
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
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(
        queryBuilder.Collections.serialize(values).queryString,
        CQLSyntax.Symbols.plus,
        column
      )
    )
  }

  def prepend(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(valueDef, CQLSyntax.Symbols.plus, column)
    )
  }

  def append(column: String, values: String*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(
        column, CQLSyntax.Symbols.plus,
        queryBuilder.Collections.serialize(values).queryString
      )
    )
  }

  def append(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(column, CQLSyntax.Symbols.plus, valueDef)
    )
  }

  def discard(column: String, values: String*): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.-,
        queryBuilder.Collections.serialize(values).queryString
      )
    )
  }

  def discard(column: String, valueDef: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(column, CQLSyntax.Symbols.-, valueDef)
    )
  }

  def add(column: String, values: Set[String]): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
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
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
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
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs).forcePad.append(
      collectionModifier(
        column,
        CQLSyntax.Symbols.plus,
        queryBuilder.Utils.map(pairs)
      )
    )
  }

  def serialize(list: Seq[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`[`)
      .append(list.mkString(", "))
      .append(CQLSyntax.Symbols.`]`)
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

  def mapType[K, V](key: Primitive[K], value: Primitive[V]): CQLQuery = {
    diamond(CQLSyntax.Collections.map, CQLQuery(
      List(frozen(key).queryString, frozen(value).queryString)
    ).queryString)
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

  def tupleType(types: String*): CQLQuery = {
    CQLQuery(CQLSyntax.Collections.tuple)
    .append(CQLSyntax.Symbols.`<`)
    .append(types)
    .append(CQLSyntax.Symbols.`>`)
  }

  def frozen[V](p: Primitive[V]): CQLQuery = frozen(p.dataType, p.shouldFreeze)

  def frozen(cassandraType: String, shouldFreeze: Boolean): CQLQuery = {
    if (shouldFreeze) {
      diamond(CQLSyntax.Collections.frozen, cassandraType)
    } else {
      CQLQuery(cassandraType)
    }
  }

  def frozen(cassandraType: String): CQLQuery = {
    diamond(CQLSyntax.Collections.frozen, cassandraType)
  }

  def collectionType(
    colType: String,
    cassandraType: String,
    shouldFreeze: Boolean,
    freezeInner: Boolean,
    static: Boolean
  ): CQLQuery = {
    val root = (shouldFreeze, freezeInner) match {
      case (true, true) =>
        // frozen<list<frozen<tuple<string, string>>>
        frozen(diamond(colType, frozen(cassandraType).queryString))
      case (true, false) =>
        // frozen<list<string>>
        frozen(diamond(colType, cassandraType))
        // list<frozen<tuple<string, string>>
      case (false, true) => diamond(colType, frozen(cassandraType).queryString)
        // list<string>
      case (false, false) => diamond(colType, cassandraType)
    }

    if (static) {
      root.pad.append("static")
    } else {
      root
    }
  }

  def frozen(cassandraType: CQLQuery): CQLQuery = {
    frozen(cassandraType.queryString)
  }

  def frozen(name: String, cassandraType: CQLQuery): CQLQuery = {
    CQLQuery(name).forcePad.append(
      diamond(CQLSyntax.Collections.frozen, cassandraType.queryString)
    )
  }
}
