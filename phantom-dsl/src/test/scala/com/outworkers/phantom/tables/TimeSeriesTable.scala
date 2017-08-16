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

case class TimeSeriesRecord(
  id: UUID,
  name: String,
  timestamp: DateTime
)

case class TimeUUIDRecord(
  user: UUID,
  id: UUID,
  name: String
) {
  def timestamp: DateTime = id.datetime
}

abstract class TimeSeriesTable extends Table[TimeSeriesTable, TimeSeriesRecord] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object timestamp extends DateTimeColumn with ClusteringOrder with Descending {
    override val name = "unixTimestamp"
  }
}

abstract class TimeUUIDTable extends Table[TimeUUIDTable, TimeUUIDRecord] {

  object user extends UUIDColumn with PartitionKey
  object id extends TimeUUIDColumn with ClusteringOrder with Descending
  object name extends StringColumn

  def retrieve(user: UUID): Future[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id ascending).fetch()
  }

  def retrieveDescending(user: UUID): Future[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id descending).fetch()
  }
}
