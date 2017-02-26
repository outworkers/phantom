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

import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

private[builder] class DeleteQueryBuilder {
  def delete(table: String): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }

  def delete(table: String, cond: CQLQuery): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(cond)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }

  def delete(table: String, conds: Seq[CQLQuery]): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(conds.map(_.queryString))
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }

  def deleteColumn(table: String, column: String): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(column)
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }

  def deleteMapColumn(table: String, column: String, key: String): CQLQuery = {
    CQLQuery(CQLSyntax.delete)
      .forcePad.append(qUtils.mapKey(column, key))
      .forcePad.append(CQLSyntax.from)
      .forcePad.append(table)
  }
}
