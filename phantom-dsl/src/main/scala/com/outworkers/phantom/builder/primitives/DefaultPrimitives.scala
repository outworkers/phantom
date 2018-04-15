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
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}
import java.util.{Date, UUID}
import java.sql.{Timestamp => JTimestamp}

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone, LocalDate => JodaLocalDate}

import scala.util.Try

trait DefaultPrimitives {

  implicit object InetAddressPrimitive extends Primitive[InetAddress] {
    override val dataType: String = CQLSyntax.Types.Inet

    override def asCql(value: InetAddress): String = CQLQuery.empty.singleQuote(value.getHostAddress)

    override def serialize(obj: InetAddress, version: ProtocolVersion): ByteBuffer = {
      nullValueCheck(obj) { i => ByteBuffer.wrap(i.getAddress) }
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): InetAddress = {
      bytes match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case _ =>
          try
            InetAddress.getByAddress(Bytes.getArray(bytes))
          catch {
            case e: UnknownHostException =>
              throw new InvalidTypeException("Invalid bytes for inet value, got " + bytes.remaining + " bytes", e)
          }
      }
    }
  }


  implicit object BigIntPrimitive extends Primitive[BigInt] {
    override val dataType: String = CQLSyntax.Types.Varint

    override def asCql(value: BigInt): String = value.toString()

    override def serialize(obj: BigInt, version: ProtocolVersion): ByteBuffer = {
      nullValueCheck(obj)(bi =>  ByteBuffer.wrap(bi.toByteArray))
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): BigInt = {
      bytes match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case _ => new BigInteger(Bytes.getArray(bytes))
      }
    }
  }

  implicit object BlobIsPrimitive extends Primitive[ByteBuffer] {
    override val dataType: String = CQLSyntax.Types.Blob

    override def asCql(value: ByteBuffer): String = Bytes.toHexString(value)

    override def serialize(obj: ByteBuffer, version: ProtocolVersion): ByteBuffer = obj

    override def deserialize(source: ByteBuffer, version: ProtocolVersion): ByteBuffer = source
  }

  implicit object LocalDateIsPrimitive extends Primitive[LocalDate] {
    override val dataType: String = CQLSyntax.Types.Timestamp

    val codec: IntPrimitive.type = IntPrimitive

    override def asCql(value: LocalDate): String = ParseUtils.quote(value.toString)

    override def serialize(obj: LocalDate, version: ProtocolVersion): ByteBuffer = {
      nullValueCheck(obj) { dt =>
        val unsigned = CodecUtils.fromSignedToUnsignedInt(dt.getDaysSinceEpoch)
        codec.serialize(unsigned, version)
      }
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): LocalDate = {
      bytes match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case _ =>
          val unsigned = codec.deserialize(bytes, version)
          val signed = CodecUtils.fromUnsignedToSignedInt(unsigned)
          LocalDate.fromDaysSinceEpoch(signed)
      }
    }
  }


  implicit object StringPrimitive extends Primitive[String] {

    private[this] val name = "UTF-8"
    val charset: Charset = Charset.forName(name)
    val encoder: CharsetEncoder = charset.newEncoder
    val decoder: CharsetDecoder = charset.newDecoder

    def asCql(value: String): String = CQLQuery.empty.singleQuote(value)

    override def dataType: String = CQLSyntax.Types.Text

    override def serialize(obj: String, version: ProtocolVersion): ByteBuffer = {
      if (obj == Primitive.nullValue) {
        Primitive.nullValue
      } else {
        ByteBuffer.wrap(obj.getBytes(charset))
      }
    }

    override def deserialize(source: ByteBuffer, version: ProtocolVersion): String = {
      source match {
        case Primitive.nullValue => Primitive.nullValue
        case bytes if bytes.remaining() == 0 => ""
        case arr @ _ => new String(Bytes.getArray(arr), charset)
      }
    }
  }

  implicit object IntPrimitive extends Primitive[Int] {

    private[this] val byteLength = 4

    def asCql(value: Int): String = value.toString

    override def dataType: String = CQLSyntax.Types.Int

    override def serialize(obj: Int, version: ProtocolVersion): ByteBuffer = {
      ByteBuffer.allocate(byteLength).putInt(0, obj)
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Int = {
      checkNullsAndLength(
        bytes,
        byteLength,
        "Invalid 32-bits integer value, expecting 4 bytes but got " + bytes.remaining()
      ) {
        case _ => bytes.getInt(bytes.position)
      }
    }
  }

  implicit object SmallIntPrimitive extends Primitive[Short] {

    private[this] val byteLength = 2

    def asCql(value: Short): String = value.toString

    override def dataType: String = CQLSyntax.Types.SmallInt

    override def serialize(obj: Short, version: ProtocolVersion): ByteBuffer = {
      ByteBuffer.allocate(byteLength).putShort(0, obj)
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Short = {
      checkNullsAndLength(
        bytes,
        byteLength,
        "Invalid 16-bits integer value, expecting 2 bytes but got " + bytes.remaining
      ) {
        case _ => bytes.getShort(bytes.position)
      }
    }
  }

  implicit object TinyIntPrimitive extends Primitive[Byte] {
    def asCql(value: Byte): String = value.toString

    override def dataType: String = CQLSyntax.Types.TinyInt

    override def serialize(obj: Byte, version: ProtocolVersion): ByteBuffer = {
      val bb = ByteBuffer.allocate(1)
      bb.put(0, obj)
      bb
    }

    override def deserialize(source: ByteBuffer, version: ProtocolVersion): Byte = {
      checkNullsAndLength(
        source,
        1,
        "Invalid 8-bits integer value, expecting 1 byte but got " + source.remaining()
      ) {
        case _ => source.get(source.position())
      }
    }
  }

  implicit object DoublePrimitive extends Primitive[Double] {

    private[this] val byteLength = 8

    def asCql(value: Double): String = value.toString

    override def dataType: String = CQLSyntax.Types.Double

    override def serialize(obj: Double, version: ProtocolVersion): ByteBuffer = {
      ByteBuffer.allocate(byteLength).putDouble(0, obj)
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Double = {
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

  implicit object LongPrimitive extends Primitive[Long] {

    private[this] val byteLength = 8

    def asCql(value: Long): String = value.toString

    override def dataType: String = CQLSyntax.Types.BigInt

    override def serialize(obj: Long, version: ProtocolVersion): ByteBuffer = {
      val bb = ByteBuffer.allocate(byteLength)
      bb.putLong(0, obj)
      bb
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Long = {
      bytes match {
        case Primitive.nullValue => 0L
        case b if b.remaining() == 0 => 0L
        case b if b.remaining() != byteLength =>
          throw new InvalidTypeException(
            "Invalid 64-bits long value, expecting 8 bytes but got " + bytes.remaining
          )
        case _ => bytes.getLong(bytes.position)
      }
    }
  }

  implicit object FloatPrimitive extends Primitive[Float] {

    private[this] val byteLength = 4

    def asCql(value: Float): String = value.toString

    override def dataType: String = CQLSyntax.Types.Float

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Float = {
      checkNullsAndLength(
        bytes,
        byteLength,
        s"Invalid 32-bits float value, expecting $byteLength bytes but got " + bytes.remaining
      ) {
        case b if b.remaining() == 0 => 0F
        case b @ _ => bytes.getFloat(b.position)
      }
    }

    override def serialize(obj: Float, version: ProtocolVersion): ByteBuffer = {
      ByteBuffer.allocate(byteLength).putFloat(0, obj)
    }
  }

  implicit object UUIDPrimitive extends Primitive[UUID] {

    private[this] val byteLength = 16

    def asCql(value: UUID): String = value.toString

    override def dataType: String = CQLSyntax.Types.UUID

    override def serialize(obj: UUID, version: ProtocolVersion): ByteBuffer = {
      nullValueCheck(obj) { value =>
        val bb = ByteBuffer.allocate(byteLength)
        bb.putLong(0, value.getMostSignificantBits)
        bb.putLong(byteLength / 2, value.getLeastSignificantBits)
        bb
      }
    }

    override def deserialize(source: ByteBuffer, version: ProtocolVersion): UUID = {
      source match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case b if b.remaining() != byteLength =>
          throw new InvalidTypeException(
            s"Invalid UUID value, expecting $byteLength bytes but got " + b.remaining
          )
        case bytes @ _ => new UUID(bytes.getLong(bytes.position()), bytes.getLong(bytes.position() + 8))
      }
    }
  }

  implicit object BooleanIsPrimitive extends Primitive[Boolean] {
    private[this] val TRUE: ByteBuffer = ByteBuffer.wrap(Array[Byte](1))
    private[this] val FALSE: ByteBuffer = ByteBuffer.wrap(Array[Byte](0))

    override val dataType: String = CQLSyntax.Types.Boolean

    def fromRow(row: GettableData, name: String): Option[Boolean] =
      if (row.isNull(name)) None else Try(row.getBool(name)).toOption

    override def asCql(value: Boolean): String = value.toString

    override def serialize(obj: Boolean, version: ProtocolVersion): ByteBuffer = {
      if (obj) TRUE.duplicate else FALSE.duplicate
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Boolean = {
      bytes match {
        case Primitive.nullValue => false
        case b if b.remaining() == 0 => false
        case b if b.remaining() != 1 =>
          throw new InvalidTypeException(
            "Invalid boolean value, expecting 1 byte but got " + bytes.remaining
          )
        case _ => bytes.get(bytes.position) != 0
      }
    }
  }

  implicit object BigDecimalIsPrimitive extends Primitive[BigDecimal] {
    override def dataType: String = CQLSyntax.Types.Decimal

    override def asCql(value: BigDecimal): String = value.toString()

    override def serialize(obj: BigDecimal, version: ProtocolVersion): ByteBuffer = {
      obj match {
        case Primitive.nullValue => Primitive.nullValue
        case _ =>
          val bi = obj.bigDecimal.unscaledValue
          val bibytes = bi.toByteArray

          val bytes = ByteBuffer.allocate(4 + bibytes.length)
          bytes.putInt(obj.scale)
          bytes.put(bibytes)
          bytes.rewind
          bytes
      }
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): BigDecimal = {
      bytes match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case b if b.remaining() < 4 =>
          throw new InvalidTypeException(
            "Invalid decimal value, expecting at least 4 bytes but got " + bytes.remaining
          )

        case _ =>
          val newBytes = bytes.duplicate
          val scale = newBytes.getInt
          val bibytes = new Array[Byte](newBytes.remaining)
          newBytes.get(bibytes)

          BigDecimal(new BigInteger(bibytes), scale)
      }
    }


  }

  val DateTimeIsPrimitive: Primitive[DateTime] = Primitive.manuallyDerive[DateTime, Long](
    _.toDateTime(DateTimeZone.UTC).getMillis,
    new DateTime(_, DateTimeZone.UTC)
  )(LongPrimitive)(CQLSyntax.Types.Timestamp)

  val SqlTimestampIsPrimitive: Primitive[JTimestamp] = Primitive.manuallyDerive[JTimestamp, Long](
    _.getTime,
    new JTimestamp(_)
  )(LongPrimitive)(CQLSyntax.Types.Timestamp)

  val JodaLocalDateIsPrimitive: Primitive[JodaLocalDate] = Primitive.manuallyDerive[JodaLocalDate, DateTime](
    jld => jld.toDateTimeAtCurrentTime(DateTimeZone.UTC), _.toLocalDate
  )(DateTimeIsPrimitive)(CQLSyntax.Types.Timestamp)

  val DateIsPrimitive: Primitive[Date] = Primitive
    .manuallyDerive[Date, Long](_.getTime, new Date(_))(LongPrimitive)(CQLSyntax.Types.Timestamp)


}
