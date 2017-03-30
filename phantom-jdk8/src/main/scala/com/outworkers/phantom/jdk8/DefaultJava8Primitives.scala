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
import java.util.Date

import com.datastax.driver.core.{GettableByIndexData, GettableByNameData}
import com.datastax.driver.core.{ LocalDate => DatastaxLocalDate }
import com.outworkers.phantom.jdk8.dsl.JdkLocalDate
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.DateTime

import scala.util.Try

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

  implicit val zonedDateTimePrimitive: Primitive[ZonedDateTime] = {
    Primitive.derive[ZonedDateTime, (Long, String)](dt => dt.toInstant.toEpochMilli -> dt.getZone.getId) {
      case (timestamp, zone) => ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of(zone))
    }
  }


  implicit val JdkLocalDateIsPrimitive: Primitive[JdkLocalDate] = {
    Primitive.derive[JdkLocalDate, DatastaxLocalDate](dt => JdkLocalDate.of(dt.getYear, dt.getMonth, dt.getDayOfWeek)) {
      case (timestamp, zone) => Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  implicit object JdkLocalDateTimeIsPrimitive extends Primitive[LocalDateTime] {
    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: LocalDateTime): String = {
      CQLQuery.empty.singleQuote(value.atZone(ZoneOffset.UTC).toString)
    }

    override def fromString(value: String): LocalDateTime = {
      Instant.ofEpochMilli(value.toLong).atZone(ZoneOffset.UTC).toLocalDateTime
    }
  }

}
