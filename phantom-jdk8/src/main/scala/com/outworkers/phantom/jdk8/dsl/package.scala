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
package com.outworkers.phantom.jdk8

import java.time.{Instant, LocalDateTime, OffsetDateTime, ZoneId}
import java.util.UUID

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.dsl.CassandraTable

package object dsl extends DefaultJava8Primitives {

  type OffsetDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, OffsetDateTime]

  type ZonedDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, ZonedDateTime]

  type JdkLocalDateColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, JdkLocalDate]

  type JdkLocalDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.PrimitiveColumn[Owner, Record, LocalDateTime]

  type OptionalOffsetDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, OffsetDateTime]

  type OptionalZonedDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, ZonedDateTime]

  type OptionalJdkLocalDateColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, JdkLocalDate]

  type OptionalJdkLocalDateTimeColumn[
  Owner <: CassandraTable[Owner, Record],
  Record
  ] = com.outworkers.phantom.column.OptionalPrimitiveColumn[Owner, Record, LocalDateTime]

  type OffsetDateTime = java.time.OffsetDateTime
  type ZonedDateTime = java.time.ZonedDateTime
  type JdkLocalDate = java.time.LocalDate
  type JdkLocalDateTime = java.time.LocalDateTime

  implicit class OffsetDateTimeHelper(val date: OffsetDateTime) extends AnyVal {
    def timeuuid: UUID = {
      new UUID(
        UUIDs.startOf(date.toInstant.toEpochMilli).getMostSignificantBits,
        scala.util.Random.nextLong()
      )
    }
  }

  implicit class TimeUUIDAugmenter(val uuid: UUID) extends AnyVal {

    def offsetDateTime(zone: String): OffsetDateTime = offsetDateTime(ZoneId.of(zone))

    def offsetDateTime(zone: ZoneId): OffsetDateTime = {
      OffsetDateTime.ofInstant(Instant.ofEpochMilli(UUIDs.unixTimestamp(uuid)), zone)
    }
  }

}
