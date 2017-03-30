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
package com.outworkers.phantom.builder.primitives

import java.math.BigInteger
import java.net.{InetAddress, UnknownHostException}
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.InvalidTypeException
import com.google.common.base.Charsets
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try

object Primitives {

    class StringPrimitive extends Primitive[String] {
      def asCql(value: String): String = CQLQuery.empty.singleQuote(value)

      override def cassandraType: String = CQLSyntax.Types.Text

      override def fromString(value: String): String = value

      override def serialize(obj: String): ByteBuffer = {
        val source = if (Option(obj).isEmpty) "NULL" else ParseUtils.quote(obj)
        ByteBuffer.wrap(source.getBytes(Charsets.UTF_8))
      }

      override def deserialize(source: ByteBuffer): String = {
        source match {
          case None.orNull => None.orNull
          case bytes if bytes.remaining() == 0 => ""
          case arr @ _ => new String(arr.array(), Charsets.UTF_8)
        }
      }
    }

    class IntPrimitive extends Primitive[Int] {

      private[this] val byteLength = 4

      def asCql(value: Int): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Int

      override def fromString(value: String): Int = value.toInt

      override def serialize(obj: Int): ByteBuffer = {
        val bb = ByteBuffer.allocate(4)
        bb.putInt(0, obj)
        bb
      }

      override def deserialize(bytes: ByteBuffer): Int = {
        checkNullsAndLength(
          bytes,
          byteLength,
          "Invalid 32-bits integer value, expecting 4 bytes but got " + bytes.remaining()
        ) {
          case _ => bytes.getShort(bytes.position)
        }
      }
    }

    class SmallIntPrimitive extends Primitive[Short] {

      private[this] val byteLength = 2

      def asCql(value: Short): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.SmallInt

      override def fromString(value: String): Short = value.toShort

      override def serialize(obj: Short): ByteBuffer = {
        val bb = ByteBuffer.allocate(2)
        bb.putShort(0, obj)
        bb
      }

      override def deserialize(bytes: ByteBuffer): Short = {
        checkNullsAndLength(
          bytes,
          byteLength,
          "Invalid 16-bits integer value, expecting 2 bytes but got " + bytes.remaining
        ) {
          case _ => bytes.getShort(bytes.position)
        }
      }
    }

    class TinyIntPrimitive extends Primitive[Byte] {
      def asCql(value: Byte): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.TinyInt

      override def fromString(value: String): Byte = value.toByte

      override def serialize(obj: Byte): ByteBuffer = {
        val bb = ByteBuffer.allocate(1)
        bb.put(0, obj)
        bb
      }

      override def deserialize(source: ByteBuffer): Byte = {
        checkNullsAndLength(
          source,
          1,
          "Invalid 8-bits integer value, expecting 1 byte but got " + source.remaining()
        ) {
          case b => source.get(source.position())
        }
      }
    }

    class DoublePrimitive extends Primitive[Double] {

      private[this] val byteLength = 8

      def asCql(value: Double): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Double

      override def fromString(value: String): Double = java.lang.Double.parseDouble(value)

      override def serialize(obj: Double): ByteBuffer = {
        val bb: ByteBuffer = ByteBuffer.allocate(byteLength)
        bb.putDouble(0, obj)
        bb
      }

      override def deserialize(bytes: ByteBuffer): Double = {
        checkNullsAndLength(
          bytes,
          byteLength,
          "Invalid 64-bits double value, expecting 8 bytes but got " + bytes.remaining
        ) {
          case b if b.remaining() == 0 => 0D
          case b @ _ => bytes.getDouble(b.position)
        }
      }
    }

    class LongPrimitive extends Primitive[Long] {

      private[this] val byteLength = 8

      def asCql(value: Long): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.BigInt

      override def fromString(value: String): Long = java.lang.Long.parseLong(value)

      override def serialize(obj: Long): ByteBuffer = {
        val bb = ByteBuffer.allocate(8)
        bb.putLong(0, obj)
        bb
      }

      override def deserialize(bytes: ByteBuffer): Long = {
        bytes match {
          case None.orNull => 0L
          case b if b.remaining() == 0 => 0L
          case b if b.remaining() != byteLength =>
            throw new InvalidTypeException(
              "Invalid 64-bits long value, expecting 8 bytes but got " + bytes.remaining
            )
          case b @ _ => bytes.getLong(bytes.position)
        }
      }
    }

    class FloatPrimitive extends Primitive[Float] {

      private[this] val byteLength = 4

      def asCql(value: Float): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Float

      override def fromString(value: String): Float = java.lang.Float.parseFloat(value)

      override def deserialize(bytes: ByteBuffer): Double = {
        checkNullsAndLength(
          bytes,
          byteLength,
          s"Invalid 32-bits float value, expecting $byteLength bytes but got " + bytes.remaining
        ) {
          case b if b.remaining() == 0 => 0D
          case b @ _ => bytes.getDouble(b.position)
        }
      }

      override def serialize(obj: Float): ByteBuffer = {
        val bb = ByteBuffer.allocate(byteLength)
        bb.putFloat(0, obj)
        bb
      }
    }

    class UUIDPrimitive extends Primitive[UUID] {

      private[this] val byteLength = 16

      def asCql(value: UUID): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.UUID

      override def fromString(value: String): UUID = UUID.fromString(value)

      override def serialize(obj: UUID): ByteBuffer = {
        nullValueCheck(obj) { value =>
          val bb = ByteBuffer.allocate(byteLength)
          bb.putLong(0, value.getMostSignificantBits)
          bb.putLong(8, value.getLeastSignificantBits)
          bb
        }
      }

      override def deserialize(source: ByteBuffer): UUID = ???
    }

    class DateIsPrimitive extends Primitive[Date] {

      val cassandraType = CQLSyntax.Types.Timestamp

      def fromRow(row: GettableData, name: String): Option[Date] =
        if (row.isNull(name)) None else Try(row.getTimestamp(name)).toOption

      override def asCql(value: Date): String = {
        DateSerializer.asCql(value)
      }

      override def fromString(value: String): Date = {
        new DateTime(value, DateTimeZone.UTC).toDate
      }
    }

    class LocalDateIsPrimitive extends Primitive[LocalDate] {
      val cassandraType = CQLSyntax.Types.Date

      def fromRow(row: GettableData, name: String): Option[LocalDate] =
        if (row.isNull(name)) None else Try(row.getDate(name)).toOption

      override def asCql(value: LocalDate): String = {
        DateSerializer.asCql(value)
      }

      override def fromString(value: String): LocalDate = {
        LocalDate.fromMillisSinceEpoch(new DateTime(value, DateTimeZone.UTC).getMillis)
      }
    }

    class JodaLocalDateIsPrimitive extends Primitive[org.joda.time.LocalDate] {
      val cassandraType = CQLSyntax.Types.Date

      override def asCql(value: org.joda.time.LocalDate): String = {
        CQLQuery.empty.singleQuote(DateSerializer.asCql(value))
      }

      override def fromString(value: String): org.joda.time.LocalDate = {
        new DateTime(value, DateTimeZone.UTC).toLocalDate
      }
    }

    class DateTimeIsPrimitive extends Primitive[DateTime] {
      val cassandraType = CQLSyntax.Types.Timestamp

      override def asCql(value: DateTime): String = {
        DateSerializer.asCql(value)
      }

      override def fromString(value: String): DateTime = new DateTime(value, DateTimeZone.UTC)
    }

    class BooleanIsPrimitive extends Primitive[Boolean] {
      private[this] val TRUE: ByteBuffer = ByteBuffer.wrap(Array[Byte](1))
      private[this] val FALSE: ByteBuffer = ByteBuffer.wrap(Array[Byte](0))

      val cassandraType = CQLSyntax.Types.Boolean

      def fromRow(row: GettableData, name: String): Option[Boolean] =
        if (row.isNull(name)) None else Try(row.getBool(name)).toOption

      override def asCql(value: Boolean): String = value.toString

      override def fromString(value: String): Boolean = value match {
        case "true" => true
        case "false" => false
        case _ => throw new Exception(s"Couldn't parse a boolean value from $value")
      }

      override def serialize(obj: Boolean): ByteBuffer = {
        if (obj) TRUE.duplicate else FALSE.duplicate
      }

      override def deserialize(bytes: ByteBuffer): Boolean = {
        bytes match {
          case None.orNull => false
          case b if b.remaining() == 0 => false
          case b if b.remaining() != 1 =>
            throw new InvalidTypeException(
              "Invalid boolean value, expecting 1 byte but got " + bytes.remaining
            )
          case b @ _ => bytes.get(bytes.position) != 0
        }
      }
    }

    class BigDecimalIsPrimitive extends Primitive[BigDecimal] {
      val cassandraType = CQLSyntax.Types.Decimal

      override def asCql(value: BigDecimal): String = value.toString()

      override def fromString(value: String): BigDecimal = BigDecimal(value)

      override def serialize(obj: BigDecimal): ByteBuffer = {
        obj match {
          case None.orNull => None.orNull
          case decimal =>
            val bi: BigInteger = obj.bigDecimal.unscaledValue
            val scale: Int = obj.scale
            val bibytes: Array[Byte] = bi.toByteArray

            val bytes: ByteBuffer = ByteBuffer.allocate(4 + bibytes.length)
            bytes.putInt(scale)
            bytes.put(bibytes)
            bytes.rewind
            bytes
        }
      }

      override def deserialize(bytes: ByteBuffer): BigDecimal = {
        bytes match {
          case None.orNull => None.orNull
          case b if b.remaining() == 0 => None.orNull
          case b if b.remaining() < 4 =>
            throw new InvalidTypeException(
              "Invalid decimal value, expecting at least 4 bytes but got " + bytes.remaining
            )

          case bt @ _ =>
            val newBytes = bytes.duplicate

            val scale: Int = bytes.getInt
            val bibytes: Array[Byte] = new Array[Byte](bytes.remaining)
            newBytes.get(bibytes)

            val bi: BigInteger = new BigInteger(bibytes)
            BigDecimal(bi, scale)
        }

      }
    }

    class InetAddressPrimitive extends Primitive[InetAddress] {
      val cassandraType = CQLSyntax.Types.Inet

      override def asCql(value: InetAddress): String = CQLQuery.empty.singleQuote(value.getHostAddress)

      override def fromString(value: String): InetAddress = InetAddress.getByName(value)

      override def serialize(obj: InetAddress): ByteBuffer = {
        nullValueCheck(obj) { i => ByteBuffer.wrap(i.getAddress) }
      }

      override def deserialize(bytes: ByteBuffer): InetAddress = {
        bytes match {
          case super.nullValue => nullValue
          case b if b.remaining() == 0 => nullValue
          case _ =>
            try
              InetAddress.getByAddress(Bytes.getArray(bytes))
            catch {
              case e: UnknownHostException =>
                throw new InvalidTypeException("Invalid bytes for inet value, got " + bytes.remaining + " bytes")
            }
        }
      }
    }

    class BigIntPrimitive extends Primitive[BigInt] {
      val cassandraType = CQLSyntax.Types.Varint

      override def asCql(value: BigInt): String = value.toString()

      override def fromString(value: String): BigInt = BigInt(value)

      override def serialize(obj: BigInt): ByteBuffer = {
        nullValueCheck(obj)(bi =>  ByteBuffer.wrap(bi.toByteArray))
      }

      override def deserialize(bytes: ByteBuffer): BigInt = {
        bytes match {
          case super.nullValue => nullValue
          case b if b.remaining() == 0 => nullValue
          case bt => new BigInteger(Bytes.getArray(bytes))
        }
      }
    }

    class BlobIsPrimitive extends Primitive[ByteBuffer] {
      val cassandraType = CQLSyntax.Types.Blob

      override def asCql(value: ByteBuffer): String = Bytes.toHexString(value)

      override def fromString(value: String): ByteBuffer = Bytes.fromHexString(value)

      override def serialize(obj: ByteBuffer): ByteBuffer = obj

      override def deserialize(source: ByteBuffer): ByteBuffer = source
    }

  def list[T : Primitive](): Primitive[List[T]] = {
    new Primitive[List[T]] {

      val ev = implicitly[Primitive[T]]

      override def cassandraType: String = QueryBuilder.Collections.listType(ev.cassandraType).queryString

      override def fromString(value: String): List[T] = value.split(",").map(Primitive[T].fromString).toList

      override def asCql(value: List[T]): String = {
        QueryBuilder.Collections
          .serialize(value.map(Primitive[T].asCql))
          .queryString
      }
    }
  }

  def set[T : Primitive](): Primitive[Set[T]] = {
    new Primitive[Set[T]] {

      val ev = implicitly[Primitive[T]]

      override def cassandraType: String = QueryBuilder.Collections.setType(ev.cassandraType).queryString

      override def fromString(value: String): Set[T] = value.split(",").map(Primitive[T].fromString).toSet

      override def asCql(value: Set[T]): String = {
        QueryBuilder.Collections.serialize(value.map(Primitive[T].asCql)).queryString
      }
    }
  }

  def map[K : Primitive, V : Primitive](): Primitive[Map[K, V]] = {
    new Primitive[Map[K, V]] {

      val keyPrimitive = implicitly[Primitive[K]]
      val valuePrimitive = implicitly[Primitive[V]]

      override def cassandraType: String = QueryBuilder.Collections.mapType(
        keyPrimitive.cassandraType,
        valuePrimitive.cassandraType
      ).queryString

      override def fromString(value: String): Map[K, V] = Map.empty[K, V]

      override def asCql(map: Map[K, V]): String = QueryBuilder.Utils.map(map.map {
        case (key, value) => Primitive[K].asCql(key) -> Primitive[V].asCql(value)
      }).queryString
    }
  }
}