/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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

case class VerizonRecord(
  uid: String,
  name: String,
  description: String,
  oid: String,
  sid: String,
  createdOn: Long,
  lastUpdated: Long,
  isDeleted: Boolean
)

abstract class VerizonSchema extends Table[VerizonSchema, VerizonRecord] {

  object uid extends StringColumn with PartitionKey

  object name extends StringColumn

  object description extends StringColumn

  object oid extends StringColumn with Index

  object sid extends StringColumn with Index

  object createdon extends LongColumn

  object lastupdated extends LongColumn

  object isdeleted extends BooleanColumn with Index

  def updateStatus(uid: String, status: Boolean): Future[ResultSet] = {
    update
      .where(_.uid eqs uid)
      .modify(_.isdeleted setTo status)
      .ifExists
      .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
      .future()
  }

  lazy val updateDeleteStatus = update
    .where(_.uid eqs ?)
    .modify(_.isdeleted setTo ?)
    .ifExists
    .consistencyLevel_=(ConsistencyLevel.LOCAL_QUORUM)
    .prepareAsync()

}
