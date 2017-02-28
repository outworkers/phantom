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

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core.{GettableByIndexData, GettableByNameData, GettableData, LocalDate}
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.JavaConverters._
import scala.util.Try

object Primitives {

    class StringPrimitive extends Primitive[String] {

      override type PrimitiveType = java.lang.String

      def asCql(value: String): String = CQLQuery.empty.singleQuote(value)

      override def cassandraType: String = CQLSyntax.Types.Text

      override def fromString(value: String): String = value

      override def fromRow(column: String, row: GettableByNameData): Try[String] = {
        nullCheck(column, row)(_.getString(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[String] = {
        nullCheck(index, row)(_.getString(index))
      }

      override def clz: Class[String] = classOf[String]
    }

    class IntPrimitive extends Primitive[Int] {

      override type PrimitiveType = java.lang.Integer

      def asCql(value: Int): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Int

      override def fromString(value: String): Int = value.toInt

      override def fromRow(column: String, row: GettableByNameData): Try[Int] = nullCheck(column, row) {
        _.getInt(column)
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Int] = {
        nullCheck(index, row)(_.getInt(index))
      }

      override def clz: Class[java.lang.Integer] = classOf[java.lang.Integer]
    }

    class SmallIntPrimitive extends Primitive[Short] {

      override type PrimitiveType = java.lang.Short

      def asCql(value: Short): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.SmallInt

      override def fromString(value: String): Short = value.toShort

      override def fromRow(column: String, row: GettableByNameData): Try[Short] = {
        nullCheck(column, row)(_.getShort(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Short] = {
        nullCheck(index, row)(_.getShort(index))
      }

      override def clz: Class[java.lang.Short] = classOf[java.lang.Short]
    }

    class TinyIntPrimitive extends Primitive[Byte] {

      override type PrimitiveType = java.lang.Byte

      def asCql(value: Byte): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.TinyInt

      override def fromString(value: String): Byte = value.toByte

      override def fromRow(column: String, row: GettableByNameData): Try[Byte] = {
        nullCheck(column, row)(_.getByte(column))
      }

      override def clz: Class[java.lang.Byte] = classOf[java.lang.Byte]

      override def fromRow(index: Int, row: GettableByIndexData): Try[Byte] = {
        nullCheck(index, row)(_.getByte(index))
      }
    }

    class DoublePrimitive extends Primitive[Double] {

      override type PrimitiveType = java.lang.Double

      def asCql(value: Double): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Double

      override def fromString(value: String): Double = value.toDouble

      override def fromRow(column: String, row: GettableByNameData): Try[Double] = {
        nullCheck(column, row)(_.getDouble(column))
      }

      override def clz: Class[java.lang.Double] = classOf[java.lang.Double]

      override def fromRow(index: Int, row: GettableByIndexData): Try[Double] = {
        nullCheck(index, row)(_.getDouble(index))
      }
    }

    class LongPrimitive extends Primitive[Long] {

      override type PrimitiveType = java.lang.Long

      def asCql(value: Long): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.BigInt

      override def fromString(value: String): Long = value.toLong

      override def fromRow(column: String, row: GettableByNameData): Try[Long] = {
        nullCheck(column, row)(_.getLong(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Long] = {
        nullCheck(index, row)(_.getLong(index))
      }

      override def clz: Class[java.lang.Long] = classOf[java.lang.Long]
    }

    class FloatPrimitive extends Primitive[Float] {

      override type PrimitiveType = java.lang.Float

      def asCql(value: Float): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.Float

      override def fromString(value: String): Float = value.toFloat

      override def fromRow(column: String, row: GettableByNameData): Try[Float] = {
        nullCheck(column, row)(r => r.getFloat(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Float] = {
        nullCheck(index, row)(_.getFloat(index))
      }

      override def clz: Class[java.lang.Float] = classOf[java.lang.Float]
    }

    class UUIDPrimitive extends Primitive[UUID] {

      override type PrimitiveType = java.util.UUID

      def asCql(value: UUID): String = value.toString

      override def cassandraType: String = CQLSyntax.Types.UUID

      override def fromString(value: String): UUID = UUID.fromString(value)

      override def fromRow(column: String, row: GettableByNameData): Try[UUID] = {
        nullCheck(column, row)(_.getUUID(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[UUID] = {
        nullCheck(index, row)(_.getUUID(index))
      }

      override def clz: Class[UUID] = classOf[UUID]
    }

    class DateIsPrimitive extends Primitive[Date] {

      override type PrimitiveType = java.util.Date

      val cassandraType = CQLSyntax.Types.Timestamp

      def fromRow(row: GettableData, name: String): Option[Date] =
        if (row.isNull(name)) None else Try(row.getTimestamp(name)).toOption

      override def asCql(value: Date): String = {
        DateSerializer.asCql(value)
      }

      override def fromRow(column: String, row: GettableByNameData): Try[Date] = {
        nullCheck(column, row)(_.getTimestamp(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Date] = {
        nullCheck(index, row)(_.getTimestamp(index))
      }

      override def fromString(value: String): Date = {
        new DateTime(value, DateTimeZone.UTC).toDate
      }

      override def clz: Class[Date] = classOf[Date]
    }

    class LocalDateIsPrimitive extends Primitive[LocalDate] {

      override type PrimitiveType = com.datastax.driver.core.LocalDate

      val cassandraType = CQLSyntax.Types.Date

      def fromRow(row: GettableData, name: String): Option[LocalDate] =
        if (row.isNull(name)) None else Try(row.getDate(name)).toOption

      override def asCql(value: LocalDate): String = {
        DateSerializer.asCql(value)
      }

      override def fromRow(column: String, row: GettableByNameData): Try[LocalDate] = {
        nullCheck(column, row)(_.getDate(column))
      }

      override def fromString(value: String): LocalDate = {
        LocalDate.fromMillisSinceEpoch(new DateTime(value, DateTimeZone.UTC).getMillis)
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[LocalDate] = {
        nullCheck(index, row)(_.getDate(index))
      }

      override def clz: Class[LocalDate] = classOf[LocalDate]
    }

    class JodaLocalDateIsPrimitive extends Primitive[org.joda.time.LocalDate] {

      override type PrimitiveType = com.datastax.driver.core.LocalDate

      val cassandraType = CQLSyntax.Types.Date

      def fromRow(row: GettableData, name: String): Option[org.joda.time.LocalDate] =
        if (row.isNull(name)) None else Try(
          new DateTime(row.getDate(name).getMillisSinceEpoch,
            DateTimeZone.UTC).toLocalDate
        ).toOption

      override def asCql(value: org.joda.time.LocalDate): String = {
        CQLQuery.empty.singleQuote(DateSerializer.asCql(value))
      }

      override def fromRow(column: String, row: GettableByNameData): Try[org.joda.time.LocalDate] = {
        nullCheck(column, row) {
          r => new DateTime(r.getDate(column).getMillisSinceEpoch, DateTimeZone.UTC).toLocalDate
        }
      }

      override def fromString(value: String): org.joda.time.LocalDate = {
        new DateTime(value, DateTimeZone.UTC).toLocalDate
      }

      override def clz: Class[com.datastax.driver.core.LocalDate] = classOf[com.datastax.driver.core.LocalDate]

      override def fromRow(index: Int, row: GettableByIndexData): Try[org.joda.time.LocalDate] = {
        nullCheck(index, row) {
          r => new DateTime(r.getDate(index).getMillisSinceEpoch, DateTimeZone.UTC).toLocalDate
        }
      }
    }

    class DateTimeIsPrimitive extends Primitive[DateTime] {

      override type PrimitiveType = java.util.Date

      val cassandraType = CQLSyntax.Types.Timestamp

      override def asCql(value: DateTime): String = {
        DateSerializer.asCql(value)
      }

      override def fromRow(column: String, row: GettableByNameData): Try[DateTime] = {
        nullCheck(column, row) {
          r => new DateTime(r.getTimestamp(column), DateTimeZone.UTC)
        }
      }

      override def fromString(value: String): DateTime = new DateTime(value, DateTimeZone.UTC)

      override def clz: Class[Date] = classOf[Date]

      override def extract(obj: PrimitiveType): DateTime = new DateTime(obj, DateTimeZone.UTC)

      override def fromRow(index: Int, row: GettableByIndexData): Try[DateTime] = {
        nullCheck(index, row) {
          r => new DateTime(r.getTimestamp(index), DateTimeZone.UTC)
        }
      }
    }


    class BooleanIsPrimitive extends Primitive[Boolean] {

      override type PrimitiveType = java.lang.Boolean

      val cassandraType = CQLSyntax.Types.Boolean

      def fromRow(row: GettableData, name: String): Option[Boolean] =
        if (row.isNull(name)) None else Try(row.getBool(name)).toOption

      override def asCql(value: Boolean): String = value.toString

      override def fromRow(column: String, row: GettableByNameData): Try[Boolean] = {
        nullCheck(column, row)(_.getBool(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Boolean] = {
        nullCheck(index, row)(_.getBool(index))
      }

      override def fromString(value: String): Boolean = value match {
        case "true" => true
        case "false" => false
        case _ => throw new Exception(s"Couldn't parse a boolean value from $value")
      }

      override def clz: Class[java.lang.Boolean] = classOf[java.lang.Boolean]
    }

    class BigDecimalIsPrimitive extends Primitive[BigDecimal] {

      override type PrimitiveType = java.math.BigDecimal

      val cassandraType = CQLSyntax.Types.Decimal

      override def fromRow(column: String, row: GettableByNameData): Try[BigDecimal] = {
        nullCheck(column, row)(r => BigDecimal(r.getDecimal(column)))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[BigDecimal] = {
        nullCheck(index, row)(r => BigDecimal(r.getDecimal(index)))
      }

      override def asCql(value: BigDecimal): String = value.toString()

      override def fromString(value: String): BigDecimal = BigDecimal(value)

      override def clz: Class[java.math.BigDecimal] = classOf[java.math.BigDecimal]

      override def extract(obj: java.math.BigDecimal): BigDecimal = BigDecimal(obj)
    }

    class InetAddressPrimitive extends Primitive[InetAddress] {

      override type PrimitiveType = java.net.InetAddress

      val cassandraType = CQLSyntax.Types.Inet

      override def fromRow(column: String, row: GettableByNameData): Try[InetAddress] = {
        nullCheck(column, row)(_.getInet(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[InetAddress] = {
        nullCheck(index, row)(_.getInet(index))
      }

      override def asCql(value: InetAddress): String = CQLQuery.empty.singleQuote(value.getHostAddress)

      override def fromString(value: String): InetAddress = InetAddress.getByName(value)

      override def clz: Class[InetAddress] = classOf[InetAddress]

    }

    class BigIntPrimitive extends Primitive[BigInt] {

      override type PrimitiveType = java.math.BigInteger

      val cassandraType = CQLSyntax.Types.Varint

      override def fromRow(column: String, row: GettableByNameData): Try[BigInt] = {
        nullCheck(column, row)(_.getVarint(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[BigInt] = {
        nullCheck(index, row)(_.getVarint(index))
      }

      override def asCql(value: BigInt): String = value.toString()

      override def fromString(value: String): BigInt = BigInt(value)

      override def clz: Class[java.math.BigInteger] = classOf[java.math.BigInteger]

    }

    class BlobIsPrimitive extends Primitive[ByteBuffer] {

      override type PrimitiveType = java.nio.ByteBuffer

      val cassandraType = CQLSyntax.Types.Blob

      override def fromRow(column: String, row: GettableByNameData): Try[ByteBuffer] = {
        nullCheck(column, row)(_.getBytes(column))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[ByteBuffer] = {
        nullCheck(index, row)(_.getBytes(index))
      }

      override def asCql(value: ByteBuffer): String = Bytes.toHexString(value)

      override def fromString(value: String): ByteBuffer = Bytes.fromHexString(value)

      override def clz: Class[java.nio.ByteBuffer] = classOf[java.nio.ByteBuffer]

    }

  def list[T : Primitive](): Primitive[List[T]] = {
    new Primitive[List[T]] {

      val ev = implicitly[Primitive[T]]

      override def fromRow(column: String, row: GettableByNameData): Try[List[T]] = {
        Try(row.getList(column, ev.clz).asScala.toList.map(ev.extract))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[List[T]] = {
        Try(row.getList(index, ev.clz).asScala.toList.map(ev.extract))
      }

      override def cassandraType: String = QueryBuilder.Collections.listType(ev.cassandraType).queryString

      override def fromString(value: String): List[T] = value.split(",").map(Primitive[T].fromString).toList

      override def asCql(value: List[T]): String = {
        QueryBuilder.Collections
          .serialize(value.map(Primitive[T].asCql))
          .queryString
      }

      override def clz: Class[List[Primitive[T]#PrimitiveType]] = classOf[List[Primitive[T]#PrimitiveType]]

      override type PrimitiveType = List[Primitive[T]#PrimitiveType]
    }
  }

  def set[T : Primitive](): Primitive[Set[T]] = {
    new Primitive[Set[T]] {

      val ev = implicitly[Primitive[T]]

      override def fromRow(column: String, row: GettableByNameData): Try[Set[T]] = {
        Try(row.getSet(column, ev.clz).asScala.toSet.map(ev.extract))
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Set[T]] = {
        Try(row.getSet(index, ev.clz).asScala.toSet.map(ev.extract))
      }

      override def cassandraType: String = QueryBuilder.Collections.setType(ev.cassandraType).queryString

      override def fromString(value: String): Set[T] = value.split(",").map(Primitive[T].fromString).toSet

      override def asCql(value: Set[T]): String = {
        QueryBuilder.Collections.serialize(value.map(Primitive[T].asCql)).queryString
      }

      override def clz: Class[Set[Primitive[T]#PrimitiveType]] = classOf[Set[Primitive[T]#PrimitiveType]]

      override type PrimitiveType = Set[Primitive[T]#PrimitiveType]
    }
  }

  def map[K : Primitive, V : Primitive](): Primitive[Map[K, V]] = {
    new Primitive[Map[K, V]] {

      val keyPrimitive = implicitly[Primitive[K]]
      val valuePrimitive = implicitly[Primitive[V]]

      override def fromRow(column: String, row: GettableByNameData): Try[Map[K, V]] = {
        Try {
          row.getMap(column, keyPrimitive.clz, valuePrimitive.clz).asScala.toMap.map {
            case (key, value) => keyPrimitive.extract(key) -> valuePrimitive.extract(value)
          }
        }
      }

      override def fromRow(index: Int, row: GettableByIndexData): Try[Map[K, V]] = {
        Try {
          row.getMap(index, keyPrimitive.clz, valuePrimitive.clz).asScala.toMap.map {
            case (key, value) => keyPrimitive.extract(key) -> valuePrimitive.extract(value)
          }
        }
      }

      override def cassandraType: String = QueryBuilder.Collections.mapType(
        keyPrimitive.cassandraType,
        valuePrimitive.cassandraType
      ).queryString

      override def fromString(value: String): Map[K, V] = Map.empty[K, V]

      override def asCql(map: Map[K, V]): String = QueryBuilder.Utils.map(map.map {
        case (key, value) => Primitive[K].asCql(key) -> Primitive[V].asCql(value)
      }).queryString

      override def clz: Class[Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]] = {
        classOf[Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]]
      }

      override type PrimitiveType = Map[Primitive[K]#PrimitiveType, Primitive[V]#PrimitiveType]
    }
  }
}