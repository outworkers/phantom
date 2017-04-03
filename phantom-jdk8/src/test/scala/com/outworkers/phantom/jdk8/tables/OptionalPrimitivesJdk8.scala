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
package com.outworkers.phantom.jdk8.tables

import java.time.{LocalDate, LocalDateTime, OffsetDateTime}

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.jdk8.dsl._

import scala.concurrent.Future

case class OptionalJdk8Row(
  pkey: String,
  offsetDateTime: Option[OffsetDateTime],
  zonedDateTime: Option[ZonedDateTime],
  localDate: Option[LocalDate],
  localDateTime: Option[LocalDateTime]
)

abstract class OptionalPrimitivesJdk8 extends CassandraTable[
  OptionalPrimitivesJdk8,
  OptionalJdk8Row
] with RootConnector {

  object pkey extends StringColumn with PartitionKey

  object offsetDateTime extends OptionalOffsetDateTimeColumn

  object zonedDateTime extends OptionalZonedDateTimeColumn

  object localDate extends OptionalJdkLocalDateColumn

  object localDateTime extends OptionalJdkLocalDateTimeColumn

  def findByPkey(pkey: String): Future[Option[OptionalJdk8Row]] = {
    select.where(_.pkey eqs pkey).one()
  }
}