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

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.jdk8._

import scala.concurrent.Future

case class OptionalJdk8Row(
  pkey: String,
  offsetDateTime: Option[OffsetDateTime],
  zonedDateTime: Option[ZonedDateTime],
  localDate: Option[LocalDate],
  localDateTime: Option[LocalDateTime]
)

abstract class OptionalPrimitivesJdk8 extends Table[
  OptionalPrimitivesJdk8,
  OptionalJdk8Row
] {

  object pkey extends StringColumn with PartitionKey

  object offsetDateTime extends OptionalCol[OffsetDateTime]

  object zonedDateTime extends OptionalCol[ZonedDateTime]

  object localDate extends OptionalCol[LocalDate]

  object localDateTime extends OptionalCol[LocalDateTime]

  def findByPkey(pkey: String): Future[Option[OptionalJdk8Row]] = {
    select.where(_.pkey eqs pkey).one()
  }
}