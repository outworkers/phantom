/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.tables

import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._
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
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending {
    override val name = "unixTimestamp"
  }

  def fromRow(row: Row): TimeSeriesRecord = {
    TimeSeriesRecord(
      id(row),
      name(row),
      timestamp(row)
    )
  }
}

abstract class ConcreteTimeSeriesTable extends TimeSeriesTable with RootConnector

sealed class TimeUUIDTable extends CassandraTable[ConcreteTimeUUIDTable, TimeUUIDRecord] {

  object user extends UUIDColumn(this) with PartitionKey[UUID]
  object id extends TimeUUIDColumn(this) with ClusteringOrder[UUID] with Descending
  object name extends StringColumn(this)

  def fromRow(row: Row): TimeUUIDRecord = {
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
