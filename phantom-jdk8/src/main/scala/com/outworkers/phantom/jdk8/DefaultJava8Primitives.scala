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

import com.datastax.driver.core.GettableData
import com.outworkers.phantom.jdk8.dsl.JdkLocalDate
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import scala.util.Try

trait DefaultJava8Primitives {

  implicit object OffsetDateTimeIsPrimitive extends Primitive[OffsetDateTime] {

    override type PrimitiveType = java.util.Date

    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: OffsetDateTime): String = {
      value.toInstant.toEpochMilli.toString
    }

    override def fromRow(column: String, row: GettableData): Try[OffsetDateTime] = nullCheck(column, row) {
      r => OffsetDateTime.ofInstant(r.getTimestamp(column).toInstant, ZoneOffset.UTC)
    }

    override def fromString(value: String): OffsetDateTime = OffsetDateTime.parse(value)

    override def clz: Class[Date] = classOf[Date]
  }

  implicit object ZonedDateTimeIsPrimitive extends Primitive[ZonedDateTime] {

    override type PrimitiveType = java.util.Date

    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: ZonedDateTime): String = {
      value.toInstant.toEpochMilli.toString
    }

    override def fromRow(column: String, row: GettableData): Try[ZonedDateTime] = nullCheck(column, row) {
      r => ZonedDateTime.ofInstant(r.getTimestamp(column).toInstant, ZoneOffset.UTC)
    }

    override def fromString(value: String): ZonedDateTime = ZonedDateTime.parse(value)

    override def clz: Class[Date] = classOf[Date]
  }

  implicit object JdkLocalDateIsPrimitive extends Primitive[JdkLocalDate] {

    override type PrimitiveType = com.datastax.driver.core.LocalDate

    val cassandraType = CQLSyntax.Types.Date

    override def asCql(value: JdkLocalDate): String = {
      CQLQuery.empty.singleQuote(value.toString)
    }

    override def fromRow(column: String, row: GettableData): Try[JdkLocalDate] = nullCheck(column, row) {
      r => LocalDate.ofEpochDay(r.getDate(column).getDaysSinceEpoch)
    }

    override def fromString(value: String): JdkLocalDate = {
      Instant.ofEpochMilli(value.toLong).atOffset(ZoneOffset.UTC).toLocalDate
    }

    override def clz: Class[com.datastax.driver.core.LocalDate] = classOf[com.datastax.driver.core.LocalDate]
  }

}
