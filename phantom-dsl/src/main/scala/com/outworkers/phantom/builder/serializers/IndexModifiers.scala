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
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.syntax.CQLSyntax

private[builder] class IndexModifiers extends BaseModifiers {

  def eqs(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.eqs, value)
  }

  def notEqs(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.notEqs, value)
  }

  def lt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lt, value)
  }

  def lte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.lte, value)
  }

  def gt(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gt, value)
  }

  def gte(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.gte, value)
  }

  def in(column: String, values: String*): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def in(column: String, mark: PrepareMark): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, mark.qb)
  }

  def in(column: String, values: List[String]): CQLQuery = {
    modifier(column, CQLSyntax.Operators.in, Utils.join(values))
  }

  def fcall(name: String, params: String*): CQLQuery = {
    CQLQuery(name).append(Utils.join(params))
  }

  def where(qb: CQLQuery, condition: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.where, condition)
  }

  def and(qb: CQLQuery, clause: CQLQuery): CQLQuery = {
    Utils.concat(qb, CQLSyntax.and, clause)
  }

  def token(clause: String): CQLQuery = {
    CQLQuery(CQLSyntax.token).pad.wrapn(clause)
  }

  def token(clauses: String*): CQLQuery = {
    CQLQuery(CQLSyntax.token).pad.wrapn(clauses)
  }

  /**
   * Creates a CONTAINS where clause applicable to SET columns.
   * @param column The name of the column in which to look for the value.
   * @param value The CQL serialized value of the element to look for in the CQL MAP.
   * @return A CQL Query wrapping the contains clause.
   */
  def contains(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.contains, value)
  }

  /**
   * Creates a CONTAINS KEY where clause applicable to Map columns.
   * @param column The name of the column in which to look for the value.
   * @param value The CQL serialized value of the element to look for in the CQL MAP.
   * @return A CQL Query wrapping the contains clause.
   */
  def containsKey(column: String, value: String): CQLQuery = {
    modifier(column, CQLSyntax.Operators.containsKey, value)
  }

  /**
    * Creates a CONTAINS ENTRY where clause applicable to Map columns.
    * @param column The name of the column in which to look for the value.
    * @param value The CQL serialized value of the element to look for in the CQL MAP.
    * @return A CQL Query wrapping the contains clause.
    */
  def containsEntry(column: String, key: String, value: String): CQLQuery = {
    modifier(QueryBuilder.Utils.mapKey(column, key).queryString, CQLSyntax.Operators.eqs, value)
  }

}
