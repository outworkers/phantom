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
package com.outworkers.phantom.tables.bugs

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class ClientKey(sessionId: UUID, key: String, other: String)

abstract class ClientKeys extends Table[ClientKeys, ClientKey] {
  object sessionId extends UUIDColumn with PartitionKey
  object key extends StringColumn with ClusteringOrder
  object other extends StringColumn

  def findById(id: UUID): Future[Option[ClientKey]] = {
    select.where(_.sessionId eqs id).one()
  }

  def insert(c: ClientKey): Future[ResultSet] = {
    insert().future()
  }
}