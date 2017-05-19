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
package com.outworkers.phantom

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.builder.clauses.OperatorClause
import com.outworkers.phantom.builder.ops.TimeUUIDOperator
import java.time.{LocalDate => JavaLocalDate, LocalDateTime => JavaLocalDateTime, _}

import com.datastax.driver.core.{LocalDate => DatastaxLocalDate}
import com.outworkers.phantom.builder.primitives.{Primitive, Primitives}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone}

package object jdk8 {

  type OffsetDateTime = java.time.OffsetDateTime
  type ZonedDateTime = java.time.ZonedDateTime
  type JdkLocalDate = java.time.LocalDate
  type JdkLocalDateTime = java.time.LocalDateTime

  implicit val OffsetDateTimeIsPrimitive: Primitive[OffsetDateTime] = {
    Primitive.derive[OffsetDateTime, (Long, String)](
      offsetDt => offsetDt.toInstant.toEpochMilli -> offsetDt.getOffset.getId
    ) { case (timestamp, zone) =>
      OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.of(zone))
    }
  }

  implicit val zonePrimitive: Primitive[ZoneId] = Primitive.derive[ZoneId, String](_.getId)(ZoneId.of)

  implicit val LocalDateIsPrimitive = Primitive.manuallyDerive[JavaLocalDate, DatastaxLocalDate](
    l => {
      val off = OffsetDateTime.of(l.atTime(0, 0), ZoneOffset.UTC)
      DatastaxLocalDate.fromYearMonthDay(off.getYear, off.getMonthValue, off.getDayOfMonth)
    }, s => {
      val conv = OffsetDateTime.ofInstant(Instant.ofEpochMilli(s.getMillisSinceEpoch), ZoneOffset.UTC)
      JavaLocalDate.of(conv.getYear, conv.getMonth, conv.getDayOfMonth)
    }

  )(Primitives.LocalDateIsPrimitive)(CQLSyntax.Types.Date)

  implicit val zonedDateTimePrimitive: Primitive[ZonedDateTime] = {
    Primitive.derive[ZonedDateTime, (Long, String)](dt => dt.toInstant.toEpochMilli -> dt.getZone.getId) {
      case (timestamp, zone) => ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zone))
    }
  }

  implicit val JdkLocalDateTimeIsPrimitive: Primitive[JavaLocalDateTime] = {
    Primitive.derive[JavaLocalDateTime, DateTime](jd =>
      new DateTime(jd.toInstant(ZoneOffset.UTC).toEpochMilli, DateTimeZone.UTC)
    )(dt => JavaLocalDateTime.ofInstant(Instant.ofEpochMilli(dt.getMillis), ZoneOffset.UTC))
  }

  @deprecated("Use Col[OffsetDateTime] without passing in the 'this' argument", "2.9.0")
  type OffsetDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.PrimitiveColumn[Owner, Record, OffsetDateTime]

  @deprecated("Use Col[ZonedDateTime] without passing in the 'this' argument", "2.9.0")
  type ZonedDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.PrimitiveColumn[Owner, Record, ZonedDateTime]

  @deprecated("Use Col[LocalDate] without passing in the 'this' argument", "2.9.0")
  type JdkLocalDateColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.PrimitiveColumn[Owner, Record, JdkLocalDate]

  @deprecated("Use Col[LocalDateTime] without passing in the 'this' argument", "2.9.0")
  type JdkLocalDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.PrimitiveColumn[Owner, Record, JdkLocalDateTime]

  @deprecated("Use OptionalCol[OffsetDateTime] without passing in the 'this' argument", "2.9.0")
  type OptionalOffsetDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.OptionalPrimitiveColumn[Owner, Record, OffsetDateTime]

  @deprecated("Use OptionalCol[ZonedDateTime] without passing in the 'this' argument", "2.9.0")
  type OptionalZonedDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.OptionalPrimitiveColumn[Owner, Record, ZonedDateTime]

  @deprecated("Use OptionalCol[LocalDate] without passing in the 'this' argument", "2.9.0")
  type OptionalJdkLocalDateColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.OptionalPrimitiveColumn[Owner, Record, JdkLocalDate]

  @deprecated("Use OptionalCol[LocalDateTime] without passing in the 'this' argument", "2.9.0")
  type OptionalJdkLocalDateTimeColumn[
    Owner <: CassandraTable[Owner, Record],
    Record
  ] = column.OptionalPrimitiveColumn[Owner, Record, JdkLocalDateTime]

  def instantToTimeuuid(instant: Instant): UUID = {
    new UUID(
      UUIDs.startOf(instant.toEpochMilli).getMostSignificantBits,
      scala.util.Random.nextLong()
    )
  }

  implicit class OffsetDateTimeHelper(val date: OffsetDateTime) extends AnyVal {
    def timeuuid: UUID = instantToTimeuuid(date.toInstant)

    def asJoda: DateTime = new DateTime(date.toInstant.toEpochMilli, DateTimeZone.UTC)
  }

  implicit class ZonedDateTimeHelper(val date: ZonedDateTime) extends AnyVal {
    def timeuuid: UUID = instantToTimeuuid(date.toInstant)

    def asJoda: DateTime = new DateTime(date.toInstant.toEpochMilli, DateTimeZone.UTC)
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
