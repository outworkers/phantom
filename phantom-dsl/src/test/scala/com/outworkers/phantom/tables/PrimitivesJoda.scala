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
package com.outworkers.phantom.tables

import com.datastax.driver.core.PagingState
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.util.testing.sample
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class JodaRow(
  pkey: String,
  intColumn: Int,
  timestamp: DateTime
)

sealed class PrimitivesJoda extends CassandraTable[ConcretePrimitivesJoda, JodaRow] {
  object pkey extends StringColumn(this) with PartitionKey[String]
  object intColumn extends IntColumn(this)
  object timestamp extends DateTimeColumn(this)

  override def fromRow(r: Row): JodaRow = extract[JodaRow](r)
}

abstract class ConcretePrimitivesJoda extends PrimitivesJoda with RootConnector {

  def store(primitive: JodaRow): InsertQuery.Default[ConcretePrimitivesJoda, JodaRow] = {
    insert.value(_.pkey, primitive.pkey)
      .value(_.intColumn, primitive.intColumn)
      .value(_.timestamp, primitive.timestamp)
  }

  def fetchPage(limit: Int, paging: Option[PagingState]): Future[ListResult[JodaRow]] = {
    select.limit(limit).paginateRecord(paging)
  }

  override val tableName = "PrimitivesJoda"

}

