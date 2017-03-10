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

import java.time._
import java.util.UUID

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.OperatorClause
import com.outworkers.phantom.builder.clauses.OperatorClause.Condition
import com.outworkers.phantom.builder.ops.{MaxTimeUUID, MinTimeUUID, TimeUUIDOperator}
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.dsl.CassandraTable
import org.joda.time.{DateTime, DateTimeZone}

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
  def instantToTimeuuid(instant: Instant): UUID = {

    new UUID(
      UUIDs.startOf(instant.toEpochMilli).getMostSignificantBits,
      scala.util.Random.nextLong()
    )
  }

  implicit class OffsetDateTimeHelper(val date: OffsetDateTime) extends AnyVal {
    def timeuuid(): UUID = instantToTimeuuid(date.toInstant)

    def asJoda(): DateTime = new DateTime(date.toInstant.toEpochMilli, DateTimeZone.UTC)
  }

  implicit class ZonedDateTimeHelper(val date: ZonedDateTime) extends AnyVal {
    def timeuuid(): UUID = instantToTimeuuid(date.toInstant)

    def asJoda(): DateTime = new DateTime(date.toInstant.toEpochMilli, DateTimeZone.UTC)
  }

  implicit class TimeUUIDAugmenter(val uuid: UUID) extends AnyVal {

    /**
      * Converts this [[UUID]] to a [[ZonedDateTime]] given a zone argument.
      * @param zone A [[String]] that is meant to contain a valid [[ZoneId]]. This method will not
      *             check if the string provided is a valid [[ZoneId]], it will just pass this through
      *             to [[ZoneId.of]].
      * @return A [[ZonedDateTime]], which contains a time instant and information about a [[ZoneId]].
      */
    def zonedDateTime(zone: String): ZonedDateTime = zonedDateTime(ZoneId.of(zone))

    /**
      * Converts this [[UUID]] to a [[ZonedDateTime]] given a zone argument.
      * @param zone A [[ZoneId]] representing a temporal zone from Java 8 API.
      * @return A [[ZonedDateTime]], which contains a time instant and information about a [[ZoneId]].
      */
    def zonedDateTime(zone: ZoneId): ZonedDateTime = {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(UUIDs.unixTimestamp(uuid)), zone)
    }

    /**
      * Converts this [[UUID]] to an [[OffsetDateTime]] given a zone argument.
      * @param zone A [[String]] that is meant to contain a valid [[ZoneId]]. This method will not
      *             check if the string provided is a valid [[ZoneId]], it will just pass this through
      *             to [[ZoneId.of]].
      * @return A [[OffsetDateTime]], which contains a time instant and offset information based on a [[ZoneId]].
      */
    def offsetDateTime(zone: String): OffsetDateTime = offsetDateTime(ZoneId.of(zone))

    /**
      * Converts this [[UUID]] to a [[ZonedDateTime]] given a zone argument.
      * @param zone A [[ZoneId]] representing a temporal zone from Java 8 API.
      * @return A [[ZonedDateTime]], which contains a time instant and information about a [[ZoneId]].
      */
    def offsetDateTime(zone: ZoneId): OffsetDateTime = {
      OffsetDateTime.ofInstant(Instant.ofEpochMilli(UUIDs.unixTimestamp(uuid)), zone)
    }
  }

  implicit class Jdk8TimeUUIDOps(val op: TimeUUIDOperator) extends AnyVal {
    def apply(odt: OffsetDateTime): OperatorClause.Condition = op(odt.asJoda)

    def apply(zdt: ZonedDateTime): OperatorClause.Condition = op(zdt.asJoda)
  }
}
