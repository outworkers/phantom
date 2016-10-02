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
package com.websudos.phantom.builder.primitives

import scala.reflect.macros.blackbox


class CachedPrimitiveTrees(val c: blackbox.Context) {

  import c.universe._

  val rowType = tq"com.datastax.driver.core.Row"
  val boolType = tq"scala.Boolean"
  val strType: Tree = tq"java.lang.String"
  val intType: Tree = tq"scala.Int"
  val byteType: Tree = tq"scala.Byte"
  val doubleType: Tree = tq"scala.Double"
  val shortType: Tree = tq"scala.Short"
  val uuidType: Tree = tq"java.util.UUID"
  val longType: Tree = tq"scala.Long"
  val floatType: Tree = tq"scala.Float"
  val dateType: Tree = tq"java.util.Date"
  val localDate: Tree = tq"com.datastax.driver.core.LocalDate"
  val dateTimeType: Tree = tq"org.joda.time.DateTime"
  val localJodaDate: Tree = tq"org.joda.time.LocalDate"

  val cql = q"com.websudos.phantom.builder.query.CQLQuery"
  val syntax = q"com.websudos.phantom.builder.syntax.CQLSyntax"

  def tryT(x: Tree): Tree = tq"scala.util.Try[$x]"

  def booleanPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$boolType] {

      override type PrimitiveType = java.lang.Boolean

      val cassandraType = $syntax.Types.Boolean

      def fromRow(row: $rowType, name: $strType): Option[$boolType] = {
        if (row.isNull(name)) None else scala.util.Try(row.getBool(name)).toOption
      }

      override def asCql(value: $boolType): $strType = value.toString

      override def fromRow(column: $strType, row: $rowType): ${tryT(boolType)} = {
        nullCheck(column, row)(_.getBool(column))
      }

      override def fromString(value: $strType): $boolType = value match {
        case "true" => true
        case "false" => false
        case _ => throw new Exception(s"Couldn't parse a boolean value from " + value)
      }

      override def clz: Class[java.lang.Boolean] = classOf[java.lang.Boolean]
    }
    """

  def stringPrimitive: c.Tree =
    q"""
      new com.websudos.phantom.builder.primitives.Primitive[$strType] {

        override type PrimitiveType = $strType

        def asCql(value: $strType): $strType = $cql.empty.singleQuote(value)

        override def cassandraType: $strType = $syntax.Types.Text

        override def fromString(value: $strType): $strType = value

        override def fromRow(column: $strType, row: $rowType): ${tryT(strType)} = {
          nullCheck(column, row)(_.getString(column))
        }

        override def clz: Class[$strType] = classOf[$strType]
      }
    """

  def intPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$intType] {

      override type PrimitiveType = java.lang.Integer

      def asCql(value: $intType): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.Int

      override def fromString(value: $strType): Int = value.toInt

      override def fromRow(column: $strType, row: $rowType): ${tryT(intType)} = {
        nullCheck(column, row)(_.getInt(column))
      }

      override def clz: Class[java.lang.Integer] = classOf[java.lang.Integer]
    }
    """

  def shortPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$shortType] {

      override type PrimitiveType = java.lang.Short

      def asCql(value: $shortType): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.SmallInt

      override def fromString(value: $strType): $shortType = value.toShort

      override def fromRow(column: $strType, row: $rowType): ${tryT(shortType)} = {
        nullCheck(column, row)(_.getShort(column))
      }

      override def clz: Class[java.lang.Short] = classOf[java.lang.Short]
    }
  """

  def bytePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$byteType] {

      override type PrimitiveType = java.lang.Byte

      def asCql(value: $byteType): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.TinyInt

      override def fromString(value: $strType): $byteType = value.toByte

      override def fromRow(column: $strType, row: $rowType): ${tryT(byteType)} = {
        nullCheck(column, row)(_.getByte(column))
      }

      override def clz: Class[java.lang.Byte] = classOf[java.lang.Byte]
    }
  """

  def doublePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$doubleType] {

      override type PrimitiveType = java.lang.Double

      def asCql(value: Double): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.Double

      override def fromString(value: $strType): $doubleType = value.toDouble

      override def fromRow(column: $strType, row: $rowType): ${tryT(doubleType)} = {
        nullCheck(column, row)(_.getDouble(column))
      }

      override def clz: Class[java.lang.Double] = classOf[java.lang.Double]
    }"""

  def longPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[Long] {

      override type PrimitiveType = java.lang.Long

      def asCql(value: $longType): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.BigInt

      override def fromString(value: $strType): Long = value.toLong

      override def fromRow(column: $strType, row: $rowType): ${tryT(longType)} = {
        nullCheck(column, row)(_.getLong(column))
      }

      override def clz: Class[java.lang.Long] = classOf[java.lang.Long]
    }
  """

  def floatPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$floatType] {

      override type PrimitiveType = java.lang.Float

      def asCql(value: $floatType): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.Float

      override def fromString(value: $strType): $floatType = value.toFloat

      override def fromRow(column: $strType, row: $rowType): ${tryT(floatType)} = {
        nullCheck(column, row)(_.getFloat(column))
      }

      override def clz: Class[java.lang.Float] = classOf[java.lang.Float]
    }
  """

  def uuidPrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$uuidType] {

      override type PrimitiveType = java.util.UUID

      def asCql(value: UUID): $strType = value.toString

      override def cassandraType: $strType = $syntax.Types.UUID

      override def fromString(value: $strType): $uuidType = java.util.UUID.fromString(value)

      override def fromRow(column: $strType, row: $rowType): ${tryT(uuidType)} = {
        nullCheck(column, row)(_.getUUID(column))
      }

      override def clz: Class[$uuidType] = classOf[$uuidType]
    }
  """

  def datePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$dateType] {

      override type PrimitiveType = java.util.Date

      val cassandraType = $syntax.Types.Timestamp

      def fromRow(row: $rowType, name: $strType): Option[$dateType] = {
        if (row.isNull(name)) None else Try(row.getTimestamp(name)).toOption
      }

      override def asCql(value: $dateType): $strType = {
        com.websudos.phantom.builder.primitives.DateSerializer.asCql(value)
      }

      override def fromRow(column: $strType, row: $rowType): ${tryT(dateType)} = {
        nullCheck(column, row)(_.getTimestamp(column))
      }

      override def fromString(value: $strType): $dateType = {
        new org.joda.time.DateTime(value, org.joda.time.DateTimeZone.UTC).toDate
      }

      override def clz: Class[$dateType] = classOf[$dateType]
    }
  """

  def localDatePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$localDate] {

      override type PrimitiveType = com.datastax.driver.core.LocalDate

      val cassandraType = $syntax.Types.Date

      override def asCql(value: $localDate): $strType = {
        com.websudos.phantom.builder.primitives.DateSerializer.asCql(value)
      }

      override def fromRow(column: $strType, row: $rowType): ${tryT(localDate)} = {
        nullCheck(column, row)(_.getDate(column))
      }

      override def fromString(value: $strType): $localDate = {
        com.datastax.driver.core.LocalDate.fromMillisSinceEpoch(
          new org.joda.time.DateTime(
            value,
            org.joda.time.DateTimeZone.UTC
          ).getMillis
        )
      }

      override def clz: Class[$localDate] = classOf[$localDate]
    }
  """

  def dateTimePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$dateTimeType] {

      override type PrimitiveType = java.util.Date

      val cassandraType = $syntax.Types.Timestamp

      override def asCql(value: $dateTimeType): $strType = {
        com.websudos.phantom.builder.primitives.DateSerializer.asCql(value)
      }

      override def fromRow(column: $strType, row: Row): ${tryT(dateTimeType)} = {
        nullCheck(column, row)(r =>
          new org.joda.time.DateTime(
            r.getTimestamp(column),
            org.joda.time.DateTimeZone.UTC
          )
        )
      }

      override def fromString(value: $strType): $dateTimeType = {
        new org.joda.time.DateTime(value, org.joda.time.DateTimeZone.UTC)
      }

      override def clz: Class[$dateType] = classOf[$dateType]

      override def extract(obj: PrimitiveType): $dateTimeType = {
        new org.joda.time.DateTime(
          obj,
          org.joda.time.DateTimeZone.UTC
        )
      }
    """

  def localJodaDatePrimitive: c.Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$localJodaDate] {

      override type PrimitiveType = com.datastax.driver.core.LocalDate

      val cassandraType = $syntax.Types.Date

      def fromRow(row: $rowType, name: $strType): Option[$localJodaDate] = {
        if (row.isNull(name)) {
          None
        } else {
          Try(
            new org.joda.time.DateTime(
              row.getDate(name).getMillisSinceEpoch,
              org.joda.time.DateTimeZone.UTC
            ).toLocalDate
          ).toOption
        }
      }

      override def asCql(value: $localJodaDate): $strType = {
        $cql.empty.singleQuote(DateSerializer.asCql(value))
      }

      override def fromRow(column: $strType, row: $rowType): Try[$localJodaDate] = {
        nullCheck(column, row) {
          r => new org.joda.time.DateTime(
            r.getDate(column).getMillisSinceEpoch,
            org.joda.time.DateTimeZone.UTC
          ).toLocalDate
        }
      }

      override def fromString(value: $strType): $localJodaDate = {
        new org.joda.time.DateTime(value, org.joda.time.DateTimeZone.UTC).toLocalDate
      }

      override def clz: Class[$localJodaDate] = classOf[$localJodaDate]
    }
    """
}