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

import java.util.UUID

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class OptionalSecondaryRecord(
  id: UUID,
  secondary: Option[Int]
)

abstract class OptionalSecondaryIndexTable extends CassandraTable[
  OptionalSecondaryIndexTable,
  OptionalSecondaryRecord
] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object secondary extends OptionalIntColumn(this) with Index

  def store(rec: OptionalSecondaryRecord): Future[ResultSet] = {
    insert.value(_.id, rec.id)
      .value(_.secondary, rec.secondary)
      .future()
  }

  def findById(id: UUID): Future[Option[OptionalSecondaryRecord]] = {
    select.where(_.id eqs id).one()
  }

  def findByOptionalSecondary(sec: Int): Future[Option[OptionalSecondaryRecord]] = {
    select.where(_.secondary eqs sec).one()
  }
}


