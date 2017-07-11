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
import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class JodaRow(
  pkey: String,
  intColumn: Int,
  timestamp: DateTime
)

abstract class PrimitivesJoda extends Table[
  PrimitivesJoda,
  JodaRow
] {
  object pkey extends StringColumn with PartitionKey
  object intColumn extends IntColumn
  object timestamp extends DateTimeColumn

  def fetchPage(limit: Int, paging: Option[PagingState]): Future[ListResult[JodaRow]] = {
    select.limit(limit).paginateRecord(paging)
  }
}
