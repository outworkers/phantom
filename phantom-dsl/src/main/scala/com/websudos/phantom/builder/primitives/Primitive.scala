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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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

import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core.{LocalDate, Row}
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.util.ByteString
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Try}


private[phantom] object DateSerializer {

  def asCql(date: Date): String = date.getTime.toString

  def asCql(date: LocalDate): String = date.getMillisSinceEpoch.toString

  def asCql(date: org.joda.time.LocalDate): String = date.toString

  def asCql(date: DateTime): String = date.getMillis.toString
}

sealed trait ValueTypeDef {
  type ValueType <: AnyRef
}

abstract class Primitive[RR] {

  type PrimitiveType

  protected[this] def nullCheck[T](column: String, row: Row)(fn: Row => T): Try[T] = {
    if (Option(row).isEmpty || row.isNull(column)) {
      Failure(new Exception(s"Column $column is null"))
    } else {
      Try(fn(row))
    }
  }

  def asCql(value: RR): String

  def cassandraType: String

  def fromRow(column: String, row: Row): Try[RR]

  def fromString(value: String): RR

  def clz: Class[PrimitiveType]

  def fromPrimitive(obj: PrimitiveType): RR = identity(obj).asInstanceOf[RR]

}

trait DefaultPrimitives {

  implicit object StringPrimitive extends Primitive[String] {

    override type PrimitiveType = java.lang.String

    def asCql(value: String): String = CQLQuery.empty.singleQuote(value)

    override def cassandraType: String = CQLSyntax.Types.Text

    override def fromString(value: String): String = value

    override def fromRow(column: String, row: Row): Try[String] = {
      nullCheck(column, row) {
        r => r.getString(column)
      }
    }

    override def clz: Class[String] = classOf[String]
  }

  implicit object IntPrimitive extends Primitive[Int] {

    override type PrimitiveType = java.lang.Integer

    def asCql(value: Int): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Int

    override def fromString(value: String): Int = value.toInt

    override def fromRow(column: String, row: Row): Try[Int] = nullCheck(column, row) {
      r => r.getInt(column)
    }

    override def clz: Class[java.lang.Integer] = classOf[java.lang.Integer]
  }

  implicit object SmallIntPrimitive extends Primitive[Short] {

    override type PrimitiveType = java.lang.Short

    def asCql(value: Short): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.SmallInt

    override def fromString(value: String): Short = value.toShort

    override def fromRow(column: String, row: Row): Try[Short] = nullCheck(column, row) {
      r => r.getShort(column)
    }

    override def clz: Class[java.lang.Short] = classOf[java.lang.Short]
  }

  implicit object TinyIntPrimitive extends Primitive[Byte] {

    override type PrimitiveType = java.lang.Byte

    def asCql(value: Byte): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.TinyInt

    override def fromString(value: String): Byte = value.toByte

    override def fromRow(column: String, row: Row): Try[Byte] = nullCheck(column, row) {
      r => r.getByte(column)
    }

    override def clz: Class[java.lang.Byte] = classOf[java.lang.Byte]
  }

  implicit object DoublePrimitive extends Primitive[Double] {

    override type PrimitiveType = java.lang.Double

    def asCql(value: Double): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Double

    override def fromString(value: String): Double = value.toDouble

    override def fromRow(column: String, row: Row): Try[Double] = nullCheck(column, row) {
      r => r.getDouble(column)
    }

    override def clz: Class[java.lang.Double] = classOf[java.lang.Double]
  }

  implicit object LongPrimitive extends Primitive[Long] {

    override type PrimitiveType = java.lang.Long

    def asCql(value: Long): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.BigInt

    override def fromString(value: String): Long = value.toLong

    override def fromRow(column: String, row: Row): Try[Long] = {
      nullCheck(column, row)(_.getLong(column))
    }

    override def clz: Class[java.lang.Long] = classOf[java.lang.Long]
  }

  implicit object FloatPrimitive extends Primitive[Float] {

    override type PrimitiveType = java.lang.Float

    def asCql(value: Float): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Float

    override def fromString(value: String): Float = value.toFloat

    override def fromRow(column: String, row: Row): Try[Float] = nullCheck(column, row) {
      r => r.getFloat(column)
    }

    override def clz: Class[java.lang.Float] = classOf[java.lang.Float]
  }

  implicit object UUIDPrimitive extends Primitive[UUID] {

    override type PrimitiveType = java.util.UUID

    def asCql(value: UUID): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.UUID

    override def fromString(value: String): UUID = UUID.fromString(value)

    override def fromRow(column: String, row: Row): Try[UUID] = nullCheck(column, row) {
      r => r.getUUID(column)
    }

    override def clz: Class[UUID] = classOf[UUID]
  }

  implicit object DateIsPrimitive extends Primitive[Date] {

    override type PrimitiveType = java.util.Date

    val cassandraType = CQLSyntax.Types.Timestamp

    def fromRow(row: Row, name: String): Option[Date] =
      if (row.isNull(name)) None else Try(row.getTimestamp(name)).toOption

    override def asCql(value: Date): String = {
      DateSerializer.asCql(value)
    }

    override def fromRow(column: String, row: Row): Try[Date] = nullCheck(column, row) {
      r => r.getTimestamp(column)
    }

    override def fromString(value: String): Date = {
      new DateTime(value, DateTimeZone.UTC).toDate
    }

    override def clz: Class[Date] = classOf[Date]
  }

  implicit object LocalDateIsPrimitive extends Primitive[LocalDate] {

    override type PrimitiveType = com.datastax.driver.core.LocalDate

    val cassandraType = CQLSyntax.Types.Date

    def fromRow(row: Row, name: String): Option[LocalDate] =
      if (row.isNull(name)) None else Try(row.getDate(name)).toOption

    override def asCql(value: LocalDate): String = {
      DateSerializer.asCql(value)
    }

    override def fromRow(column: String, row: Row): Try[LocalDate] = nullCheck(column, row) {
      r => r.getDate(column)
    }

    override def fromString(value: String): LocalDate = {
      LocalDate.fromMillisSinceEpoch(new DateTime(value, DateTimeZone.UTC).getMillis)
    }

    override def clz: Class[LocalDate] = classOf[LocalDate]
  }

  implicit object JodaLocalDateIsPrimitive extends Primitive[org.joda.time.LocalDate] {

    override type PrimitiveType = com.datastax.driver.core.LocalDate

    val cassandraType = CQLSyntax.Types.Date

    def fromRow(row: Row, name: String): Option[org.joda.time.LocalDate] =
      if (row.isNull(name)) None else Try(new DateTime(row.getDate(name).getMillisSinceEpoch).toLocalDate).toOption

    override def asCql(value: org.joda.time.LocalDate): String = {
      CQLQuery.empty.singleQuote(DateSerializer.asCql(value))
    }

    override def fromRow(column: String, row: Row): Try[org.joda.time.LocalDate] = nullCheck(column, row) {
      r => new DateTime(r.getDate(column).getMillisSinceEpoch).toLocalDate
    }

    override def fromString(value: String): org.joda.time.LocalDate = {
      new DateTime(value, DateTimeZone.UTC).toLocalDate
    }

    override def clz: Class[com.datastax.driver.core.LocalDate] = classOf[com.datastax.driver.core.LocalDate]
  }

  implicit object DateTimeIsPrimitive extends Primitive[DateTime] {

    override type PrimitiveType = java.util.Date

    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(value: DateTime): String = {
      DateSerializer.asCql(value)
    }

    override def fromRow(column: String, row: Row): Try[DateTime] = nullCheck(column, row) {
      r => new DateTime(r.getTimestamp(column))
    }

    override def fromString(value: String): DateTime = new DateTime(value)

    override def clz: Class[Date] = classOf[Date]

    override def fromPrimitive(obj: PrimitiveType): DateTime = new DateTime(obj)
  }


  implicit object BooleanIsPrimitive extends Primitive[Boolean] {

    override type PrimitiveType = java.lang.Boolean

    val cassandraType = CQLSyntax.Types.Boolean

    def fromRow(row: Row, name: String): Option[Boolean] =
      if (row.isNull(name)) None else  Try(row.getBool(name)).toOption

    override def asCql(value: Boolean): String = value.toString

    override def fromRow(column: String, row: Row): Try[Boolean] = nullCheck(column, row) {
      r => r.getBool(column)
    }

    override def fromString(value: String): Boolean = value match {
      case "true" => true
      case "false" => false
      case _ => throw new Exception(s"Couldn't parse a boolean value from $value")
    }

    override def clz: Class[java.lang.Boolean] = classOf[java.lang.Boolean]
  }

  implicit object BigDecimalPrimitive extends Primitive[BigDecimal] {

    override type PrimitiveType = java.math.BigDecimal

    val cassandraType = CQLSyntax.Types.Decimal

    override def fromRow(column: String, row: Row): Try[BigDecimal] = nullCheck(column, row) {
      r => r.getDecimal(column)
    }

    override def asCql(value: BigDecimal): String = value.toString()

    override def fromString(value: String): BigDecimal = BigDecimal(value)

    override def clz: Class[java.math.BigDecimal] = classOf[java.math.BigDecimal]
  }

  implicit object InetAddressPrimitive extends Primitive[InetAddress] {

    override type PrimitiveType = java.net.InetAddress

    val cassandraType = CQLSyntax.Types.Inet

    override def fromRow(column: String, row: Row): Try[InetAddress] = nullCheck(column, row) {
      r => r.getInet(column)
    }

    override def asCql(value: InetAddress): String = CQLQuery.empty.singleQuote(value.getHostAddress)

    override def fromString(value: String): InetAddress = InetAddress.getByName(value)

    override def clz: Class[InetAddress] = classOf[InetAddress]
  }

  implicit object BigIntPrimitive extends Primitive[BigInt] {

    override type PrimitiveType = java.math.BigInteger

    val cassandraType = CQLSyntax.Types.Varint

    override def fromRow(column: String, row: Row): Try[BigInt] = nullCheck(column, row) {
      r => r.getVarint(column)
    }

    override def asCql(value: BigInt): String = value.toString()

    override def fromString(value: String): BigInt = BigInt(value)

    override def clz: Class[java.math.BigInteger] = classOf[java.math.BigInteger]
  }

  implicit object BlobIsPrimitive extends Primitive[ByteBuffer] {

    override type PrimitiveType = java.nio.ByteBuffer

    val cassandraType = CQLSyntax.Types.Blob

    override def fromRow(column: String, row: Row): Try[ByteBuffer] = nullCheck(column, row) {
      r => r.getBytes(column)
    }

    override def asCql(value: ByteBuffer): String = Bytes.toHexString(value)

    override def fromString(value: String): ByteBuffer = Bytes.fromHexString(value)

    override def clz: Class[java.nio.ByteBuffer] = classOf[java.nio.ByteBuffer]
  }

  implicit object ByteStringPrimitive extends Primitive[ByteString] {
    override type PrimitiveType = java.nio.ByteBuffer

    val cassandraType = CQLSyntax.Types.Blob

    override def fromRow(column: String, row: Row): Try[ByteString] = nullCheck(column, row) {
      r => ByteString(r.getBytes(column))
    }

    override def asCql(value: ByteString): String = Bytes.toHexString(value.asByteBuffer)

    override def fromString(value: String): ByteString = ByteString(Bytes.fromHexString(value))

    override def clz: Class[java.nio.ByteBuffer] = classOf[java.nio.ByteBuffer]

  }
}

object Primitive extends DefaultPrimitives {
  def apply[RR : Primitive]: Primitive[RR] = implicitly[Primitive[RR]]
}
