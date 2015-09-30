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
package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}
import com.websudos.phantom.connectors.KeySpace

class TruncateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](table: Table, val qb: CQLQuery, override val consistencyLevel: Option[ConsistencyLevel] = None) extends ExecutableStatement {

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): TruncateQuery[Table, Record, Specified] = {
    if (session.v3orNewer) {
      new TruncateQuery(table, qb, Some(level))
    } else {
      new TruncateQuery(table, QueryBuilder.consistencyLevel(qb, level.toString), None)
    }
  }
}


object TruncateQuery {

  type Default[T <: CassandraTable[T, _], R] = TruncateQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): TruncateQuery.Default[T, R] = {
    new TruncateQuery(table, QueryBuilder.truncate(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }

}
