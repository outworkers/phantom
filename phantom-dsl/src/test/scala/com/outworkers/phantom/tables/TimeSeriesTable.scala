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

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.Future

case class TimeSeriesRecord(
  id: UUID,
  name: String,
  timestamp: DateTime
)

case class TimeUUIDRecord(
  user: UUID,
  id: UUID,
  name: String,
  timestamp: DateTime
)

sealed class TimeSeriesTable extends CassandraTable[ConcreteTimeSeriesTable, TimeSeriesRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Descending {
    override val name = "unixTimestamp"
  }
}

abstract class ConcreteTimeSeriesTable extends TimeSeriesTable with RootConnector

sealed class TimeUUIDTable extends CassandraTable[ConcreteTimeUUIDTable, TimeUUIDRecord] {

  object user extends UUIDColumn(this) with PartitionKey
  object id extends TimeUUIDColumn(this) with ClusteringOrder with Descending
  object name extends StringColumn(this)

  override def fromRow(row: Row): TimeUUIDRecord = {
    TimeUUIDRecord(
      user(row),
      id(row),
      name(row),
      id(row).datetime
    )
  }
}

abstract class ConcreteTimeUUIDTable extends TimeUUIDTable with RootConnector {

  def store(rec: TimeUUIDRecord): InsertQuery.Default[ConcreteTimeUUIDTable, TimeUUIDRecord] = {
    insert
      .value(_.user, rec.user)
      .value(_.id, rec.id)
      .value(_.name, rec.name)
  }

  def retrieve(user: UUID): Future[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id ascending).fetch()
  }

  def retrieveDescending(user: UUID): Future[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id descending).fetch()
  }
}
