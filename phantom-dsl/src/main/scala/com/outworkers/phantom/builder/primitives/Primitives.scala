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
import java.nio.charset.Charset
import java.nio.{BufferUnderflowException, ByteBuffer}
import java.util.{Date, UUID}

import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.{DriverInternalError, InvalidTypeException}
import com.datastax.driver.core.utils.Bytes
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone, LocalDate => JodaLocalDate}

import scala.collection.generic.CanBuildFrom
import scala.util.Try

object Utils {
  private[phantom] def unsupported(version: ProtocolVersion): DriverInternalError = {
    new DriverInternalError(s"Unsupported protocol version $version")
  }

  private[this] val baseColSize = 4

  private[this] def sizeOfValue(value: ByteBuffer, version: ProtocolVersion): Int = {
    version match {
      case ProtocolVersion.V1 | ProtocolVersion.V2 =>
        val elemSize = value.remaining

        if (elemSize > 65535) {
          throw new IllegalArgumentException(
            s"Native protocol version $version supports only elements " +
              s"with size up to 65535 bytes - but element size is $elemSize bytes"
          )
        }

        2 + elemSize
      case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 =>
        if (value == Primitive.nullValue) baseColSize else baseColSize + value.remaining

      case _ => throw unsupported(version)
    }
  }

  private[this] def sizeOfCollectionSize(version: ProtocolVersion): Int = version match {
    case ProtocolVersion.V1 | ProtocolVersion.V2 => 2
    case ProtocolVersion.V3 | ProtocolVersion.V4 | ProtocolVersion.V5 => baseColSize
    case _ => throw unsupported(version)
  }

  /**
    * Utility method that "packs" together a list of {@link ByteBuffer}s containing
    * serialized collection elements.
    * Mainly intended for use with collection codecs when serializing collections.
    *
    * @param buffers  the collection elements
    * @param elements the total number of elements
    * @param version  the protocol version to use
    * @return The serialized collection
    */
  def pack(
    buffers: Array[ByteBuffer],
    elements: Int,
    version: ProtocolVersion
  ): ByteBuffer = {
    val size = buffers.foldLeft(0)((acc, b) => acc + sizeOfValue(b, version))

    val result = ByteBuffer.allocate(sizeOfCollectionSize(version) + size)

    CodecUtils.writeSize(result, elements, version)

    for (bb <- buffers) CodecUtils.writeValue(result, bb, version)
    result.flip.asInstanceOf[ByteBuffer]
  }

  /**
    * Utility method that "packs" together a list of {@link ByteBuffer}s containing
    * serialized collection elements.
    * Mainly intended for use with collection codecs when serializing collections.
    *
    * @param buffers  the collection elements
    * @param elements the total number of elements
    * @param version  the protocol version to use
    * @return The serialized collection
    */
  def pack[M[X] <: Traversable[X]](
    buffers: M[ByteBuffer],
    elements: Int,
    version: ProtocolVersion
  ): ByteBuffer = {
    val size = buffers.foldLeft(0)((acc, b) => acc + sizeOfValue(b, version))

    val result = ByteBuffer.allocate(sizeOfCollectionSize(version) + size)

    CodecUtils.writeSize(result, elements, version)

    for (bb <- buffers) CodecUtils.writeValue(result, bb, version)
    result.flip.asInstanceOf[ByteBuffer]
  }
}

object Primitives {

  private[phantom] def emptyCollection: ByteBuffer = ByteBuffer.allocate(0)

  object StringPrimitive extends Primitive[String] {

    private[this] val name = "UTF-8"
    val charset = Charset.forName(name)
    val encoder = charset.newEncoder
    val decoder = charset.newDecoder

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

  object IntPrimitive extends Primitive[Int] {

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

  object SmallIntPrimitive extends Primitive[Short] {

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

  object TinyIntPrimitive extends Primitive[Byte] {
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
        case b => source.get(source.position())
      }
    }
  }

  object DoublePrimitive extends Primitive[Double] {

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

  object LongPrimitive extends Primitive[Long] {

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
        case b @ _ => bytes.getLong(bytes.position)
      }
    }
  }

  object FloatPrimitive extends Primitive[Float] {

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

  object UUIDPrimitive extends Primitive[UUID] {

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

  object BooleanIsPrimitive extends Primitive[Boolean] {
    private[this] val TRUE: ByteBuffer = ByteBuffer.wrap(Array[Byte](1))
    private[this] val FALSE: ByteBuffer = ByteBuffer.wrap(Array[Byte](0))

    override val dataType = CQLSyntax.Types.Boolean

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
        case b @ _ => bytes.get(bytes.position) != 0
      }
    }
  }

  object BigDecimalIsPrimitive extends Primitive[BigDecimal] {
    override def dataType: String = CQLSyntax.Types.Decimal

    override def asCql(value: BigDecimal): String = value.toString()

    override def serialize(obj: BigDecimal, version: ProtocolVersion): ByteBuffer = {
      obj match {
        case Primitive.nullValue => Primitive.nullValue
        case decimal =>
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

        case bt @ _ =>
          val newBytes = bytes.duplicate
          val scale = newBytes.getInt
          val bibytes = new Array[Byte](newBytes.remaining)
          newBytes.get(bibytes)

          BigDecimal(new BigInteger(bibytes), scale)
      }
    }
  }

  object InetAddressPrimitive extends Primitive[InetAddress] {
    override val dataType = CQLSyntax.Types.Inet

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
              throw new InvalidTypeException("Invalid bytes for inet value, got " + bytes.remaining + " bytes")
          }
      }
    }
  }

  object BigIntPrimitive extends Primitive[BigInt] {
    override val dataType = CQLSyntax.Types.Varint

    override def asCql(value: BigInt): String = value.toString()

    override def serialize(obj: BigInt, version: ProtocolVersion): ByteBuffer = {
      nullValueCheck(obj)(bi =>  ByteBuffer.wrap(bi.toByteArray))
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): BigInt = {
      bytes match {
        case Primitive.nullValue => Primitive.nullValue
        case b if b.remaining() == 0 => Primitive.nullValue
        case bt => new BigInteger(Bytes.getArray(bytes))
      }
    }
  }

  object BlobIsPrimitive extends Primitive[ByteBuffer] {
    override val dataType = CQLSyntax.Types.Blob

    override def asCql(value: ByteBuffer): String = Bytes.toHexString(value)

    override def serialize(obj: ByteBuffer, version: ProtocolVersion): ByteBuffer = obj

    override def deserialize(source: ByteBuffer, version: ProtocolVersion): ByteBuffer = source
  }

  object LocalDateIsPrimitive extends Primitive[LocalDate] {
    override val dataType = CQLSyntax.Types.Timestamp

    val codec = IntPrimitive

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
        case b @ _ =>
          val unsigned = codec.deserialize(bytes, version)
          val signed = CodecUtils.fromUnsignedToSignedInt(unsigned)
          LocalDate.fromDaysSinceEpoch(signed)
      }
    }
  }

  val DateTimeIsPrimitive: Primitive[DateTime] = Primitive.manuallyDerive[DateTime, Long](
    _.toDateTime(DateTimeZone.UTC).getMillis,
    new DateTime(_, DateTimeZone.UTC)
  )(LongPrimitive)(CQLSyntax.Types.Timestamp)

  val JodaLocalDateIsPrimitive: Primitive[JodaLocalDate] = Primitive.manuallyDerive[JodaLocalDate, DateTime](
    jld => jld.toDateTimeAtCurrentTime(DateTimeZone.UTC), _.toLocalDate
  )(DateTimeIsPrimitive)(CQLSyntax.Types.Timestamp)

  val DateIsPrimitive: Primitive[Date] = Primitive
    .manuallyDerive[Date, Long](_.getTime, new Date(_))(LongPrimitive)(CQLSyntax.Types.Timestamp)

  private[this] def collectionPrimitive[M[X] <: TraversableOnce[X], RR](
    cType: String,
    converter: M[RR] => String
  )(
    implicit ev: Primitive[RR],
    cbf: CanBuildFrom[Nothing, RR, M[RR]]
  ): Primitive[M[RR]] = new Primitive[M[RR]] {
    override def frozen: Boolean = true

    override def shouldFreeze: Boolean = true

    override def asCql(value: M[RR]): String = converter(value)

    override val dataType = cType

    override def serialize(coll: M[RR], version: ProtocolVersion): ByteBuffer = {
      coll match {
        case Primitive.nullValue => Primitive.nullValue
        case c if c.isEmpty => Utils.pack(new Array[ByteBuffer](coll.size), coll.size, version)
        case _ =>
          val bbs = coll.foldLeft(Seq.empty[ByteBuffer]) { (acc, elt) =>
            notNull(elt, "Collection elements cannot be null")
            acc :+ ev.serialize(elt, version)
          }

          Utils.pack(bbs, coll.size, version)
      }
    }

    override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): M[RR] = {
      val empty = cbf().result()
      if (bytes == Primitive.nullValue || bytes.remaining() == 0) {
         empty
      } else {
        try {
          val input = bytes.duplicate()
          val size = CodecUtils.readSize(input, version)
          val coll = cbf()
          coll.sizeHint(size)

          for (i <- 0 until size) {
            val databb = CodecUtils.readValue(input, version)
            coll += ev.deserialize(databb, version)
          }
          coll.result()
        } catch {
          case e: BufferUnderflowException =>
            throw new InvalidTypeException("Not enough bytes to deserialize collection", e)
        }
      }
    }
  }

  def list[T]()(implicit ev: Primitive[T]): Primitive[List[T]] = {
    collectionPrimitive[List, T](
      QueryBuilder.Collections.listType(ev.cassandraType).queryString,
      value => QueryBuilder.Collections.serialize(value.map(ev.asCql)).queryString
    )
  }

  def set[T]()(implicit ev: Primitive[T]): Primitive[Set[T]] = {
    collectionPrimitive[Set, T](
      QueryBuilder.Collections.setType(ev.cassandraType).queryString,
      value => QueryBuilder.Collections.serialize(value.map(ev.asCql)).queryString
    )
  }

  def option[T : Primitive]: Primitive[Option[T]] = {
    val ev = implicitly[Primitive[T]]

    val nullString = "null"

    new Primitive[Option[T]] {

      def serialize(obj: Option[T], protocol: ProtocolVersion): ByteBuffer = {
        obj.fold(
          Primitive.nullValue.asInstanceOf[ByteBuffer]
        )(ev.serialize(_, protocol))
      }

      def deserialize(source: ByteBuffer, protocol: ProtocolVersion): Option[T] = {
        if (source == Primitive.nullValue) {
          None
        } else {
          Some(ev.deserialize(source, protocol))
        }
      }

      override def dataType: String = ev.dataType

      override def asCql(value: Option[T]): String = {
        value.map(ev.asCql).getOrElse(nullString)
      }
    }
  }

  def map[K, V](implicit kp: Primitive[K], vp: Primitive[V]): Primitive[Map[K, V]] = {
    new Primitive[Map[K, V]] {
      override def frozen: Boolean = true
      override def shouldFreeze: Boolean = true

      override def dataType: String = QueryBuilder.Collections.mapType(kp, vp).queryString

      override def asCql(sourceMap: Map[K, V]): String = QueryBuilder.Utils.map(sourceMap.map {
        case (key, value) => kp.asCql(key) -> vp.asCql(value)
      }).queryString

      override def serialize(source: Map[K, V], version: ProtocolVersion): ByteBuffer = {
        source match {
          case Primitive.nullValue => emptyCollection
          case s if s.isEmpty => Utils.pack(new Array[ByteBuffer](2 * source.size), source.size, version)
          case _ =>
            val bbs = source.foldLeft(Seq.empty[ByteBuffer]) { case (acc, (key, value)) =>
              notNull(key, "Map keys cannot be null")
              acc :+ kp.serialize(key, version) :+ vp.serialize(value, version)
            }
            Utils.pack(bbs, source.size, ProtocolVersion.V4)
        }
      }

      override def deserialize(bytes: ByteBuffer, version: ProtocolVersion): Map[K, V] = {
        bytes match {
          case Primitive.nullValue => Map.empty[K, V]
          case b if b.remaining() == 0 => Map.empty[K, V]
          case _ =>
            try {
              val input = bytes.duplicate()
              val n = CodecUtils.readSize(input, version)

              val m = Map.newBuilder[K, V]

              for (i <- 0 until n) {
                val kbb = CodecUtils.readValue(input, version)
                val vbb = CodecUtils.readValue(input, version)

                m += (kp.deserialize(kbb, version) -> vp.deserialize(vbb, version))
              }
              m result()
            } catch {
              case e: BufferUnderflowException =>
                throw new InvalidTypeException("Not enough bytes to deserialize a map", e)
            }
        }
      }
    }
  }
}
