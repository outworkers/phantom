/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
import java.nio.{BufferUnderflowException, ByteBuffer}
import java.nio.charset.{Charset, CharsetDecoder, CharsetEncoder}
import java.sql.{Timestamp => JTimestamp}
import java.util.regex.Pattern
import java.util.{Date, UUID}

import com.datastax.driver.core._
import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.utils.Bytes
import com.outworkers.phantom.Row
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitives.emptyCollection
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone, LocalDate => JodaLocalDate}

import scala.annotation.implicitNotFound
import scala.util.control.NoStackTrace
import scala.util.{Failure, Try}

private[phantom] object DateSerializer {

  def asCql(date: Date): String = date.getTime.toString

  def asCql(date: LocalDate): String = date.getMillisSinceEpoch.toString

  def asCql(date: org.joda.time.LocalDate): String = date.toString

  def asCql(date: DateTime): String = date.getMillis.toString
}

@implicitNotFound(msg = "Type ${RR} must be a pre-defined Cassandra primitive.")
abstract class Primitive[RR] {

  protected[this] def notNull[T](value: T, msg: String = "Value cannot be null"): Unit = {
    if (Option(value).isEmpty) throw new NullPointerException(msg)
  }

  protected[this] def nullValueCheck(source: RR)(fn: RR => ByteBuffer): ByteBuffer = {
    if (source == Primitive.nullValue) Primitive.nullValue else fn(source)
  }

  protected[this] def checkNullsAndLength[T](
    source: ByteBuffer,
    len: Int,
    msg: String
  )(pf: PartialFunction[ByteBuffer, T]): T = {
    source match {
      case Primitive.nullValue => Primitive.nullValue.asInstanceOf[T]
      case b if b.remaining() != len => throw new InvalidTypeException(s"Expected $len, but got ${b.remaining()}. $msg")
      case bytes @ _ => pf(bytes)
    }
  }

  protected[this] def nullCheck[T](
    index: Int,
    row: Row
  )(fn: Row => T): Try[T] = {
    if (row == Primitive.nullValue) {
      Failure(new Exception(s"Row for column $index is null") with NoStackTrace)
    } else if (row.isNull(index)) {
      if (row.getColumnDefinitions.getType(index).isCollection) {
        Try(fn(row))
      } else {
        Failure(new Exception(s"Column $index is null") with NoStackTrace)
      }
    } else {
      Try(fn(row))
    }
  }

  protected[this] def nullCheck[T](
    column: String,
    row: Row
  )(fn: Row => T): Try[T] = {

    if (row == Primitive.nullValue) {
      Failure(new Exception(s"Row for column $column is null") with NoStackTrace)
    } else if (row.isNull(column)) {
      if (row.getColumnDefinitions.getType(column).isCollection) {
        Try(fn(row))
      } else {
        Failure(new Exception(s"Column $column is null") with NoStackTrace)
      }
    } else {
      Try(fn(row))
    }
  }

  /**
    * Converts the type to a CQL compatible string.
    * The primitive is responsible for handling all aspects of adequate escaping as well.
    * This is used to generate the final queries from domain objects.
    * @param value The strongly typed value.
    * @return The string representation of the value with respect to CQL standards.
    */
  def asCql(value: RR): String

  def dataType: String

  def cassandraType: String = if (frozen) {
    QueryBuilder.Collections.frozen(dataType).queryString
  } else {
    dataType
  }

  def serialize(obj: RR, protocol: ProtocolVersion): ByteBuffer

  def deserialize(source: ByteBuffer, protocol: ProtocolVersion): RR

  def fromRow(column: String, row: Row): Try[RR] = {
    nullCheck(column, row)(r => deserialize(r.getBytesUnsafe(column), r.version))
  }

  def fromRow(index: Int, row: Row): Try[RR] = {
    nullCheck(index, row)(r => deserialize(r.getBytesUnsafe(index), r.version))
  }

  /**
    * There are several kinds of primitives that must freeze in both scenarios:
    * - Set columns
    * - List columns
    * - Map columns
    * - Tuple columns
    * - UDT columns
    * @return A boolean that marks if this should be frozen.
    */
  def frozen: Boolean = false

  /**
    * Whether or not this primitive should freeze if used together with a primary column.
    * or if used as part of a partition column.
    * @return A Boolean marking whether or not this should freeze.
    */
  def shouldFreeze: Boolean = false
}

object Primitive {

  val nullValue: Null = None.orNull

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


  implicit object AsciPrimitive extends Primitive[AsciiValue] {
    private val ASCII_PATTERN = Pattern.compile("^\\p{ASCII}*$")

    /**
      * Converts the type to a CQL compatible string.
      * The primitive is responsible for handling all aspects of adequate escaping as well.
      * This is used to generate the final queries from domain objects.
      *
      * @param value The strongly typed value.
      * @return The string representation of the value with respect to CQL standards.
      */
    override def asCql(value: AsciiValue): String = StringPrimitive.asCql(value.value)

    override def dataType: String = CQLSyntax.Types.Ascii

    override def serialize(value: AsciiValue, protocol: ProtocolVersion): ByteBuffer = {
      if (Option(value.value).isDefined && !ASCII_PATTERN.matcher(value.value).matches) {
        throw new InvalidTypeException(String.format("%s is not a valid ASCII String", value))
      } else {
        StringPrimitive.serialize(value.value, protocol)
      }
    }

    override def deserialize(source: ByteBuffer, protocol: ProtocolVersion): AsciiValue = {
      AsciiValue(StringPrimitive.deserialize(source, protocol))
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

  implicit val DateTimeIsPrimitive: Primitive[DateTime] = Primitive.manuallyDerive[DateTime, Long](
    _.toDateTime(DateTimeZone.UTC).getMillis,
    new DateTime(_, DateTimeZone.UTC)
  )(LongPrimitive)(CQLSyntax.Types.Timestamp)

  implicit val SqlTimestampIsPrimitive: Primitive[JTimestamp] = Primitive.manuallyDerive[JTimestamp, Long](
    _.getTime,
    new JTimestamp(_)
  )(LongPrimitive)(CQLSyntax.Types.Timestamp)

  implicit val JodaLocalDateIsPrimitive: Primitive[JodaLocalDate] = Primitive.manuallyDerive[JodaLocalDate, DateTime](
    jld => jld.toDateTimeAtCurrentTime(DateTimeZone.UTC), _.toLocalDate
  )(DateTimeIsPrimitive)(CQLSyntax.Types.Timestamp)

  implicit val DateIsPrimitive: Primitive[Date] = Primitive
    .manuallyDerive[Date, Long](_.getTime, new Date(_))(LongPrimitive)(CQLSyntax.Types.Timestamp)


  implicit def map[K, V](implicit kp: Primitive[K], vp: Primitive[V]): Primitive[Map[K, V]] = {
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

              for (_ <- 0 until n) {
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

  def enumByIndex[En <: Enumeration](enum: En)(
    implicit ev: Primitive[Int]
  ): Primitive[En#Value] = {
    Primitive.manuallyDerive[En#Value, Int](_.id, enum(_))(ev)()
  }

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * If this does not highlight properly in your IDE, fear not, it works on my machine :)
    * @tparam T The type parameter to materialise a primitive for.
    * @return A concrete instance of a primitive, materialised via implicit blackbox macros.
    */
  implicit def materializer[T]: Primitive[T] = macro PrimitiveMacro.materializer[T]

  def iso[A, B : Primitive](r: B => A)(w: A => B): Primitive[A] = derive[A, B](w)(r)

  /**
    * Derives primitives and encodings for a non standard type.
    * @param to The function that converts a [[Target]] instance to a [[Source]] instance.
    * @param from The function that converts a [[Source]] instance to a [[Target]] instance.
    * @tparam Target The type you want to derive a primitive for.
    * @tparam Source The source type of the primitive, must already have a primitive defined for it.
    * @return A new primitive that can interact with the target type.
    */
  def derive[Target, Source : Primitive](to: Target => Source)(from: Source => Target): Primitive[Target] = {
    val primitive = implicitly[Primitive[Source]]

    new Primitive[Target] {

      override def frozen = primitive.frozen

      override def asCql(value: Target): String = primitive.asCql(to(value))

      override def dataType: String = primitive.dataType

      override def serialize(obj: Target, protocol: ProtocolVersion): ByteBuffer = {
        primitive.serialize(to(obj), protocol)
      }

      override def deserialize(source: ByteBuffer, protocol: ProtocolVersion): Target = {
        from(primitive.deserialize(source, protocol))
      }
    }
  }

  def json[Target](to: Target => String)(from: String => Target)(
    implicit ev: Primitive[String]
  ): Primitive[Target] = {
    derive[Target, String](to)(from)
  }

  /**
    * Derives a primitive without implicit lookup in phantom itself.
    * This is because the macro that empowers the implicit lookup for primitives
    * cannot be used in the same compilation as the one its defined in.
    * @param to The function that converts the derived value to the original one.
    * @param from The function that will convert an original value to a derived one.
    * @param ev Evidence that the source type is a Cassandra primitive.
    * @tparam Target The target type of the new primitive.
    * @tparam Source The type we are deriving from.
    * @return A new primitive for the target type.
    */
  def manuallyDerive[Target, Source](
    to: Target => Source,
    from: Source => Target
  )(ev: Primitive[Source])(tpe: String = ev.dataType): Primitive[Target] = {
    new Primitive[Target] {

      override def frozen: Boolean = ev.frozen

      override def shouldFreeze: Boolean = ev.shouldFreeze

      override def asCql(value: Target): String = ev.asCql(to(value))

      override def dataType: String = tpe

      override def serialize(obj: Target, protocol: ProtocolVersion): ByteBuffer = {
        ev.serialize(to(obj), protocol)
      }

      override def deserialize(source: ByteBuffer, protocol: ProtocolVersion): Target = {
        from(ev.deserialize(source, protocol))
      }
    }
  }

  /**
    * Convenience method to materialise the context bound and return a reference to it.
    * This is somewhat shorter syntax than using implicitly.
    * @tparam RR The type of the primitive to retrieve.
    * @return A reference to a concrete materialised implementation of a primitive for the given type.
    */
  def apply[RR]()(implicit ev: Primitive[RR]): Primitive[RR] = ev
}
