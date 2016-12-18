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

import com.datastax.driver.core.{GettableByIndexData, GettableByNameData, GettableData}
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

    override def fromRow(column: String, row: GettableByNameData): Try[OffsetDateTime] = nullCheck(column, row) {
      r => OffsetDateTime.ofInstant(r.getTimestamp(column).toInstant, ZoneOffset.UTC)
    }

    override def fromRow(index: Int, row: GettableByIndexData): Try[OffsetDateTime] = nullCheck(index, row) {
      r => OffsetDateTime.ofInstant(r.getTimestamp(index).toInstant, ZoneOffset.UTC)
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

    override def fromRow(column: String, row: GettableByNameData): Try[ZonedDateTime] = nullCheck(column, row) {
      r => ZonedDateTime.ofInstant(r.getTimestamp(column).toInstant, ZoneOffset.UTC)
    }

    override def fromRow(index: Int, row: GettableByIndexData): Try[ZonedDateTime] = nullCheck(index, row) {
      r => ZonedDateTime.ofInstant(r.getTimestamp(index).toInstant, ZoneOffset.UTC)
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

    override def fromRow(column: String, row: GettableByNameData): Try[JdkLocalDate] = nullCheck(column, row) {
      r => LocalDate.ofEpochDay(r.getDate(column).getDaysSinceEpoch)
    }

    override def fromRow(index: Int, row: GettableByIndexData): Try[JdkLocalDate] = nullCheck(index, row) {
      r => LocalDate.ofEpochDay(r.getDate(index).getDaysSinceEpoch)
    }

    override def fromString(value: String): JdkLocalDate = {
      Instant.ofEpochMilli(value.toLong).atOffset(ZoneOffset.UTC).toLocalDate
    }

    override def clz: Class[com.datastax.driver.core.LocalDate] = classOf[com.datastax.driver.core.LocalDate]
  }

  implicit object JdkLocalDateTimeIsPrimitive extends Primitive[LocalDateTime] {

    override type PrimitiveType = java.time.LocalDateTime

    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: LocalDateTime): String = {
      CQLQuery.empty.singleQuote(value.atZone(ZoneOffset.UTC).toString)
    }

    override def fromRow(column: String, row: GettableByNameData): Try[LocalDateTime] = nullCheck(column, row) {
      r => LocalDateTime.ofInstant(Instant.ofEpochMilli(r.getTimestamp(column).getTime), ZoneOffset.UTC)
    }

    override def fromRow(index: Int, row: GettableByIndexData): Try[LocalDateTime] = nullCheck(index, row) {
      r => LocalDateTime.ofInstant(Instant.ofEpochMilli(r.getTimestamp(index).getTime), ZoneOffset.UTC)
    }

    override def fromString(value: String): LocalDateTime = {
      Instant.ofEpochMilli(value.toLong).atZone(ZoneOffset.UTC).toLocalDateTime
    }

    override def clz: Class[LocalDateTime] = classOf[LocalDateTime]
  }

}
