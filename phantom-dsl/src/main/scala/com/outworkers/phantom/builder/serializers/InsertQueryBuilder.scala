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

import com.outworkers.phantom.builder.query.CQLQuery
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

  def columns(list: List[CQLQuery]): CQLQuery = {
    CQLQuery.empty.wrapn(list.map(_.queryString))
  }

  def values(list: List[CQLQuery]): CQLQuery = {
    CQLQuery(CQLSyntax.values).wrapn(list.map(_.queryString))
  }

}
