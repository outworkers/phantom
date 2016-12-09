/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.jdk8

import java.time._
import java.util.Date

import com.datastax.driver.core.GettableData
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.jdk8.dsl.{JdkLocalDate, JdkLocalDateTime, OffsetDateTime, ZonedDateTime}

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

  implicit object JdkLocalDateTimeIsPrimitive extends Primitive[JdkLocalDateTime] {

    override type PrimitiveType = java.time.LocalDateTime

    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: JdkLocalDateTime): String = {
      CQLQuery.empty.singleQuote(value.atZone(ZoneOffset.UTC).toString)
    }

    override def fromRow(column: String, row: GettableData): Try[JdkLocalDateTime] = nullCheck(column, row) {
      r => LocalDateTime.ofInstant(Instant.ofEpochMilli(r.getTimestamp(column).getTime), ZoneOffset.UTC)
    }

    override def fromString(value: String): JdkLocalDateTime = {
      Instant.ofEpochMilli(value.toLong).atZone(ZoneOffset.UTC).toLocalDateTime
    }

    override def clz: Class[JdkLocalDateTime] = classOf[JdkLocalDateTime]
  }

}
