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

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import macrocompat.bundle
import org.joda.time.DateTime

import scala.language.experimental.macros

@bundle
class PrimitiveMacro(val c: scala.reflect.macros.blackbox.Context) {
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
  val bigDecimalType: Tree = tq"scala.math.BigDecimal"
  val inetType: Tree = tq"java.net.InetAddress"
  val bigIntType = tq"scala.math.BigInt"
  val bufferType = tq"java.nio.ByteBuffer"

  val cql = q"com.websudos.phantom.builder.query.CQLQuery"
  val syntax = q"com.websudos.phantom.builder.syntax.CQLSyntax"

  def tryT(x: Tree): Tree = tq"scala.util.Try[$x]"

  def typed[A : c.WeakTypeTag] = weakTypeOf[A].typeSymbol

  def materializer[T : c.WeakTypeTag]: c.Expr[Primitive[T]] = {
    val tpe = weakTypeOf[T].typeSymbol

    val primitiveTree: Tree = if (tpe == typed[String]) {
      stringPrimitive
    } else if (tpe == typed[Int]) {
      intPrimitive
    } else if (tpe == typed[Byte]) {
      bytePrimitive
    } else if (tpe == typed[Boolean]) {
      booleanPrimitive
    } else if (tpe == typed[Short]) {
      shortPrimitive
    } else if (tpe == typed[Long]) {
      longPrimitive
    } else if (tpe == typed[Double]) {
      doublePrimitive
    } else if (tpe == typed[Float]) {
      floatPrimitive
    } else if (tpe == typed[UUID]) {
      uuidPrimitive
    } else if (tpe == typed[Date]) {
      datePrimitive
    } else if (tpe == typed[DateTime]) {
      dateTimePrimitive
    } else if (tpe == typed[com.datastax.driver.core.LocalDate]) {
      localDatePrimitive
    } else if (tpe == typed[org.joda.time.LocalDate]) {
      localJodaDatePrimitive
    } else if (tpe == typed[InetAddress]) {
      inetPrimitive
    } else if (tpe == typed[BigDecimal]) {
      bigDecimalPrimitive
    } else if (tpe == typed[BigInt]) {
      bigIntPrimitive
    } else if (tpe == typed[ByteBuffer]) {
      bufferPrimitive
    } else if (tpe == typed[Enumeration#Value]) {
      enumPrimitive[T]()
    } else {
      c.abort(c.enclosingPosition, s"Cannot find primitive implemention for $tpe")
    }

    c.Expr[Primitive[T]](primitiveTree)
  }

  val booleanPrimitive: Tree = q"""
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

  val stringPrimitive: Tree =
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

  val intPrimitive: Tree = q"""
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

  val shortPrimitive: Tree = q"""
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

  val bytePrimitive: Tree = q"""
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

  val doublePrimitive: Tree = q"""
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

  val longPrimitive: Tree = q"""
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

  val floatPrimitive: Tree = q"""
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

  val uuidPrimitive: Tree = q"""
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

  val datePrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$dateType] {

      override type PrimitiveType = java.util.Date

      val cassandraType = $syntax.Types.Timestamp

      def fromRow(row: $rowType, name: $strType): Option[$dateType] = {
        if (row.isNull(name)) None else scala.util.Try(row.getTimestamp(name)).toOption
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

  val localDatePrimitive: Tree = q"""
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

  val dateTimePrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$dateTimeType] {

      override type PrimitiveType = java.util.Date

      val cassandraType = $syntax.Types.Timestamp

      override def asCql(value: $dateTimeType): $strType = {
        com.websudos.phantom.builder.primitives.DateSerializer.asCql(value)
      }

      override def fromRow(column: $strType, row: $rowType): ${tryT(dateTimeType)} = {
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
    }"""

  val localJodaDatePrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$localJodaDate] {

      override type PrimitiveType = com.datastax.driver.core.LocalDate

      val cassandraType = $syntax.Types.Date

      def fromRow(row: $rowType, name: $strType): Option[$localJodaDate] = {
        if (row.isNull(name)) {
          None
        } else {
          scala.util.Try(
            new org.joda.time.DateTime(
              row.getDate(name).getMillisSinceEpoch,
              org.joda.time.DateTimeZone.UTC
            ).toLocalDate
          ).toOption
        }
      }

      override def asCql(value: $localJodaDate): $strType = {
        $cql.empty.singleQuote(com.websudos.phantom.builder.primitives.DateSerializer.asCql(value))
      }

      override def fromRow(column: $strType, row: $rowType): ${tryT(localJodaDate)} = {
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

      override def clz: Class[$localDate] = classOf[$localDate]
    }
    """

  val bigDecimalPrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$bigDecimalType] {

      override type PrimitiveType = java.math.BigDecimal

      val cassandraType = $syntax.Types.Decimal

      override def fromRow(column: $strType, row: $rowType): ${tryT(bigDecimalType)} = {
        nullCheck(column, row)(r => scala.math.BigDecimal(r.getDecimal(column)))
      }

      override def asCql(value: $bigDecimalType): String = value.toString()

      override def fromString(value: String): $bigDecimalType = scala.math.BigDecimal(value)

      override def clz: Class[java.math.BigDecimal] = classOf[java.math.BigDecimal]

      override def extract(obj: java.math.BigDecimal): $bigDecimalType = scala.math.BigDecimal(obj)
    }"""

  val inetPrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$inetType] {

      override type PrimitiveType = java.net.InetAddress

      val cassandraType = $syntax.Types.Inet

      override def fromRow(column: $strType, row: $rowType): ${tryT(inetType)} = {
        nullCheck(column, row)(_.getInet(column))
      }

      override def asCql(value: $inetType): $strType = $cql.empty.singleQuote(value.getHostAddress)

      override def fromString(value: $strType): $inetType = java.net.InetAddress.getByName(value)

      override def clz: Class[$inetType] = classOf[$inetType]
    }
    """

  val bigIntPrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$bigIntType] {

      override type PrimitiveType = java.math.BigInteger

      val cassandraType = $syntax.Types.Varint

      override def fromRow(column: $strType, row: $rowType): ${tryT(bigIntType)} = {
        nullCheck(column, row)(_.getVarint(column))
      }

      override def asCql(value: $bigIntType): $strType = value.toString()

      override def fromString(value: $strType): $bigIntType = scala.math.BigInt(value)

      override def clz: Class[java.math.BigInteger] = classOf[java.math.BigInteger]
    }
  """

  val bufferPrimitive: Tree = q"""
    new com.websudos.phantom.builder.primitives.Primitive[$bufferType] {

      override type PrimitiveType = java.nio.ByteBuffer

      val cassandraType = $syntax.Types.Blob

      override def fromRow(column: $strType, row: $rowType): ${tryT(bufferType)} = {
        nullCheck(column, row)(_.getBytes(column))
      }

      override def asCql(value: $bufferType): $strType = {
        com.datastax.driver.core.utils.Bytes.toHexString(value)
      }

      override def fromString(value: $strType): $bufferType = {
        com.datastax.driver.core.utils.Bytes.fromHexString(value)
      }

      override def clz: Class[java.nio.ByteBuffer] = classOf[java.nio.ByteBuffer]
    }
    """

  def enumPrimitive[T]()(implicit tag: WeakTypeTag[T]): Tree = {

    val tpe = tag.tpe
    val comp = c.parse(s"${tag.tpe.toString.replace("#Value", "")}")

    val tree = q""" new com.websudos.phantom.builder.primitives.Primitive[$tpe] {
      val strP = implicitly[com.websudos.phantom.builder.primitives.Primitive[String]]

      override type PrimitiveType = java.lang.String

      override def cassandraType: $strType = strP.cassandraType

      override def fromRow(name: $strType, row: $rowType): scala.util.Try[$tpe] = {
        nullCheck(name, row) {
          r => $comp.values.find(_.toString == r.getString(name)) match {
            case Some(value) => value
            case _ => throw new Exception("Value not found in enumeration") with scala.util.control.NoStackTrace
          }
        }
      }

      override def asCql(value: $tpe): String = {
        strP.asCql(value.toString)
      }

      override def fromString(value: $strType): $tpe = {
        $comp.values.find(value == _.toString).getOrElse(None.orNull)
      }

      override def clz: Class[$strType] = classOf[$strType]
    }"""

    println(showCode(tree))
    tree
  }
}
