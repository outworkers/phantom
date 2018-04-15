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

import java.time.{LocalDate => JavaLocalDate, LocalDateTime => JavaLocalDateTime, _}

import com.datastax.driver.core.{LocalDate => DatastaxLocalDate}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone}

package object indexed {

  implicit val OffsetDateTimeIsPrimitive: Primitive[OffsetDateTime] = Primitive.manuallyDerive[OffsetDateTime, Long](
    _.toInstant.toEpochMilli,
    millis => OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
  )(Primitive[Long])(CQLSyntax.Types.Timestamp)


  implicit val instantPrimitive: Primitive[Instant] = Primitive.manuallyDerive[Instant, Long](
    _.toEpochMilli,
    Instant.ofEpochMilli
  )(Primitives.LongPrimitive)(CQLSyntax.Types.Timestamp)

  implicit val zonePrimitive: Primitive[ZoneId] = Primitive.derive[ZoneId, String](_.getId)(ZoneId.of)

  implicit val LocalDateIsPrimitive: Primitive[JavaLocalDate] = Primitive.manuallyDerive[JavaLocalDate, DatastaxLocalDate](
    l => {
      val off = OffsetDateTime.of(l.atTime(0, 0), ZoneOffset.UTC)
      DatastaxLocalDate.fromYearMonthDay(off.getYear, off.getMonthValue, off.getDayOfMonth)
    }, s => {
      val conv = OffsetDateTime.ofInstant(Instant.ofEpochMilli(s.getMillisSinceEpoch), ZoneOffset.UTC)
      JavaLocalDate.of(conv.getYear, conv.getMonth, conv.getDayOfMonth)
    }

  )(Primitive[DatastaxLocalDate])(CQLSyntax.Types.Date)

  implicit val zonedDateTimePrimitive: Primitive[ZonedDateTime] = Primitive.manuallyDerive[ZonedDateTime, Instant](
    _.toInstant,
    ZonedDateTime.ofInstant(_, ZoneOffset.UTC)
  )(instantPrimitive)(CQLSyntax.Types.Timestamp)

  implicit val JdkLocalDateTimeIsPrimitive: Primitive[JavaLocalDateTime] = {
    Primitive.derive[JavaLocalDateTime, DateTime](jd =>
      new DateTime(jd.toInstant(ZoneOffset.UTC).toEpochMilli, DateTimeZone.UTC)
    )(dt => JavaLocalDateTime.ofInstant(Instant.ofEpochMilli(dt.getMillis), ZoneOffset.UTC))
  }

}
