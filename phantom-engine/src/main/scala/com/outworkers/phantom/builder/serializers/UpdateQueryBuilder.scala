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

private[builder] class UpdateQueryBuilder {

  def onlyIf(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.IF).forcePad.append(clause)
  }

  val ifExists: CQLQuery = CQLQuery(CQLSyntax.ifExists)

  private[this] def counterSetter(column: String, op: String, value: String): CQLQuery = {
    CQLQuery(column).forcePad.append(CQLSyntax.Symbols.eqs)
      .forcePad.append(column)
      .forcePad.append(op)
      .forcePad.append(value)
  }

  def increment(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.plus, value)
  }

  def decrement(column: String, value: String): CQLQuery = {
    counterSetter(column, CQLSyntax.Symbols.-, value)
  }

  def setTo(column: String, value: String): CQLQuery = {
    Utils.concat(column, CQLSyntax.Symbols.eqs, value)
  }

  def set(clause: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.set).forcePad.append(clause)
  }

  def where(condition: CQLQuery): CQLQuery = {
   Utils.operator(CQLSyntax.where, condition)
  }

  def and(condition: CQLQuery): CQLQuery = {
    Utils.operator(CQLSyntax.and, condition)
  }

  def clauses(clauses: Seq[CQLQuery], sep: String = " "): CQLQuery = {
    CQLQuery.empty.append(clauses.map(_.queryString).mkString(sep))
  }

  def chain(clauses: Seq[CQLQuery]): CQLQuery = {
    CQLQuery.empty.append(clauses.map(_.queryString).mkString(", "))
  }

  def usingPart(queries: Seq[CQLQuery]): CQLQuery = {
    CQLQuery(CQLSyntax.using)
      .forcePad
      .append(clauses(queries, " " + CQLSyntax.And + " "))

  }

  def update(tableName: String): CQLQuery = {
    CQLQuery(CQLSyntax.update)
      .forcePad.append(tableName)
  }

  def updateMapColumn(column: String, key: String, value: String): CQLQuery = {
    qUtils.mapKey(column, key)
      .forcePad.append(CQLSyntax.Symbols.eqs)
      .forcePad.append(value)
  }
}

