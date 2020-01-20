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
package com.outworkers.phantom.jdk8.tables

import java.time.{LocalDate => _, OffsetDateTime => _, ZonedDateTime => _, _}

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.jdk8._

import scala.concurrent.Future

case class OptionalJdk8Row(
  pkey: String,
  offsetDateTime: Option[OffsetDateTime],
  zonedDateTime: Option[ZonedDateTime],
  localDate: Option[JdkLocalDate],
  localDateTime: Option[JdkLocalDateTime]
)

object ExtendedJdk8Primitives {

  implicit val tpPrimitive: Primitive[OffsetDateTime] = OffsetDateTimeIsPrimitive()(Primitive[(Long, String)]())
  implicit val zonedDt: Primitive[ZonedDateTime] = zonedDateTimePrimitive()(Primitive[(Long, String)]())
}

import ExtendedJdk8Primitives._

abstract class OptionalPrimitivesJdk8 extends Table[
  OptionalPrimitivesJdk8,
  OptionalJdk8Row
] {

  object pkey extends StringColumn with PartitionKey

  object offsetDateTime extends OptionalCol[OffsetDateTime]()

  object zonedDateTime extends OptionalCol[ZonedDateTime]

  object localDate extends OptionalCol[JdkLocalDate]

  object localDateTime extends OptionalCol[JdkLocalDateTime]

  def findByPkey(pkey: String): Future[Option[OptionalJdk8Row]] = {
    select.where(_.pkey eqs pkey).one()
  }
}

case class User(id: Int, time: Instant)

abstract class UserSchema extends Table[UserSchema, User] {
  object id    extends IntColumn with PartitionKey
  object time  extends Col[Instant]
}
