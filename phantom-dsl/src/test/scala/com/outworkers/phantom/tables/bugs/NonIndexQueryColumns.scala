/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.tables.bugs

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

final case class TokenRecord(
  email: String,
  token: UUID,
  counter: Int
)

abstract class TokensTable extends Table[TokensTable, TokenRecord] {
  object email extends StringColumn
  object token extends UUIDColumn with PartitionKey {
    override val name = "tokenuid"
  }
  object counter extends IntColumn

  def findById(id: UUID): Future[Option[TokenRecord]] = {
    select.where( _.token eqs id).one()
  }

  def deleteById(id: UUID) : Future[ResultSet] = {
    delete.where( _.token eqs id).future()
  }

  def expired(counter: Int): Future[List[TokenRecord]] = {

    /* PHANTOM NOT IMPLEMENTED FEATURE:
     * With "allow filtering" Cassandra allows to query
     * non indexed column. Restrictions are on querying part of multi column
     * partition key. You need Cassandra 3.10+ to be able to filter
     * on partition key columns.
     */
    select.where(_.counter gte counter).allowFiltering().fetch()
  }
}