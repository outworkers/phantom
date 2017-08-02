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

import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

private[builder] trait Utils {

  def ignoreNulls(): CQLQuery = {
    CQLQuery(CQLSyntax.ignoreNulls)
  }

  def concat(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    qb.forcePad.append(clause)
  }

  def concat(qb: CQLQuery, op: String, clause: CQLQuery): CQLQuery = {
    qb.pad.append(op).forcePad.append(clause)
  }

  def operator(op: String, clause: CQLQuery): CQLQuery = {
    CQLQuery(op).forcePad.append(clause)
  }

  def concat(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).pad.append(op).forcePad.append(value)
  }

  def option(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).append(op).forcePad.append(value)
  }

  def join(list: TraversableOnce[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`(`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`)`)
  }

  def join(qbs: CQLQuery*): CQLQuery = {
    CQLQuery(qbs.map(_.queryString).mkString(", "))
  }

  def set(list: Set[String]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).append(list.mkString(", ")).append(CQLSyntax.Symbols.`}`)
  }

  def map(list: TraversableOnce[(String, String)]): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`)
      .append(list.map { case (key, value) => s"$key : $value" }.mkString(", "))
      .append(CQLSyntax.Symbols.`}`)
  }

  def curlyWrap(qb: String): CQLQuery = {
    CQLQuery(CQLSyntax.Symbols.`{`).append(qb).append(CQLSyntax.Symbols.`}`)
  }

  def curlyWrap(qb: CQLQuery): CQLQuery = curlyWrap(qb.queryString)

  /**
    * A custom series of options that is used to serialize keyspace creation queries.
    * for every instance where serializers are needed to produce {'a': 'b', 'c': 2} type queries.
    * @param clauses The set of CQL clauses, pre-serialised to their 'a': 'b' form.
    * @param sep A separator to use during query creation.
    * @return A CQL query of the specified format.
    */
  def options(clauses: Seq[CQLQuery], sep: String = ", "): CQLQuery = {
    Utils.curlyWrap(clauses.map(_.queryString).mkString(sep))
  }

  /**
    * Serializes the CQL definition of a map key based on a column and a key value.
    * When this method is called, the key should be already serialized using the Primitive API.
    * It will take 2 strings and produce an output of the following type:
    *
    * {{{
    *   QueryBuilder.Utils.mapKey("col", "test") == "col['test']"
    * }}}
    *
    * @param column The name of the column.
    * @param key The value of the key, pre-escaped and converted from a CQL Primitive to a string serialization.
    * @return
    */
  def mapKey(column: String, key: String): CQLQuery = {
    CQLQuery(column).append(CQLSyntax.Symbols.`[`)
      .append(key).append(CQLSyntax.Symbols.`]`)
  }

  def tableOption(option: String, value: String): CQLQuery = {
    Utils.concat(option, CQLSyntax.Symbols.eqs, value)
  }

  def tableOption(option: String, value: CQLQuery): CQLQuery = {
    tableOption(option, value.queryString)
  }
}

private[builder] object qUtils extends Utils
