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
import com.outworkers.phantom.builder.primitives.{Primitive, Primitives}
import org.joda.time.{DateTime, DateTimeZone}

trait DefaultJava8Primitives {

  implicit val OffsetDateTimeIsPrimitive: Primitive[OffsetDateTime] = {
    Primitive.derive[OffsetDateTime, (Long, String)](
      offsetDt =>
        offsetDt.toInstant.toEpochMilli -> offsetDt.getOffset.getId
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

  )(Primitives.LocalDateIsPrimitive)

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
}
