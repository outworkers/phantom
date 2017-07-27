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

import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class OAuth2Session(
  user_id: UUID,
  client_id: UUID,
  access_token: UUID,
  refresh_token: UUID,
  authorization_grant: UUID,
  remembered_token: Option[UUID],
  timestamp: DateTime
)

/**
 * This table is used for the internal application.
 * If a session is found in this table for a given user, it means the respective user is authenticated.
 */
abstract class SessionsByUserId extends Table[
  SessionsByUserId,
  OAuth2Session
] {

  object user_id extends UUIDColumn with PartitionKey
  object client_id extends UUIDColumn
  object access_token extends UUIDColumn
  object refresh_token extends UUIDColumn
  object authorization_grant extends UUIDColumn
  object remembered_token extends OptionalUUIDColumn
  object timestamp extends DateTimeColumn

  def findById(id: UUID): Future[Option[OAuth2Session]] = {
    select.where(_.user_id eqs id).one()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete.where(_.user_id eqs id).future()
  }
}
