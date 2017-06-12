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

private[phantom] trait InsertQueryBuilder {
  def insert(table: String): CQLQuery = {
    CQLQuery(CQLSyntax.insert)
      .forcePad.append(CQLSyntax.into)
      .forcePad.append(table)
  }

  def insert(table: CQLQuery): CQLQuery = {
    insert(table.queryString)
  }

  /**
   * Creates a CQL 2.2 JSON insert clause using a pre-serialized JSON string.
   * @param init The initialization query of the Insert clause, generally comprising the "INSERT INTO tableName" part.
   * @param jsonString The pre-serialized JSON string to insert into the Cassandra table.
   * @return A CQL query with the JSON prefix appended to the insert.
   */
  def json(init: CQLQuery, jsonString: String): CQLQuery = {
    init.pad.append("JSON").pad.append(CQLQuery.escape(jsonString))
  }

  def columns(seq: Seq[CQLQuery]): CQLQuery = {
    CQLQuery.empty.wrapn(seq.map(_.queryString))
  }

  def values(seq: Seq[CQLQuery]): CQLQuery = {
    CQLQuery(CQLSyntax.values).wrapn(seq.map(_.queryString))
  }

}
