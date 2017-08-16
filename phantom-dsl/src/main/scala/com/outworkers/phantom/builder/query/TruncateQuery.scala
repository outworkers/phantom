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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery
import com.outworkers.phantom.builder.{ConsistencyBound, QueryBuilder, Specified, Unspecified}
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}

class TruncateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](
  table: Table,
  val init: CQLQuery,
  val options: QueryOptions,
  protected[phantom] val usingPart: UsingPart = UsingPart.empty
) extends RootQuery[Table, Record, Status] {

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): TruncateQuery[Table, Record, Specified] = {
    if (session.protocolConsistency) {
      new TruncateQuery(table, init, options.consistencyLevel_=(level))
    } else {
      new TruncateQuery(table, init, options, usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  def qb: CQLQuery = usingPart build init

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options)
}


object TruncateQuery {

  type Default[T <: CassandraTable[T, _], R] = TruncateQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): TruncateQuery.Default[T, R] = {
    new TruncateQuery(
      table,
      QueryBuilder.truncate(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString),
      QueryOptions.empty
    )
  }

}
