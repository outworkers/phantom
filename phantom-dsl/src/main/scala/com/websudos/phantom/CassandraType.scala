/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.Row
import org.joda.time.DateTime

import scala.util.Try

trait CassandraWrites[T] {

  def toCType(v: T): AnyRef
  def cassandraType: String
}

trait CassandraPrimitive[T] extends CassandraWrites[T] {

  def cls: Class[_]

  def toCType(v: T): AnyRef = v.asInstanceOf[AnyRef]
  def fromCType(c: AnyRef): T = c.asInstanceOf[T]
  def fromRow(row: Row, name: String): Option[T]
}

object CassandraPrimitive {

  def apply[T: CassandraPrimitive]: CassandraPrimitive[T] = implicitly[CassandraPrimitive[T]]

  implicit object IntIsCassandraPrimitive extends CassandraPrimitive[Int] {

    val cassandraType = "int"
    def cls: Class[_] = classOf[java.lang.Integer]
    def fromRow(row: Row, name: String): Option[Int] =
      if (row.isNull(name)) None else Try(row.getInt(name)).toOption
  }

  implicit object FloatIsCassandraPrimitive extends CassandraPrimitive[Float] {

    val cassandraType = "float"
    def cls: Class[_] = classOf[java.lang.Float]
    def fromRow(row: Row, name: String): Option[Float] =
      if (row.isNull(name)) None else Try(row.getFloat(name)).toOption
  }

  implicit object LongIsCassandraPrimitive extends CassandraPrimitive[Long] {

    val cassandraType = "bigint"
    def cls: Class[_] = classOf[java.lang.Long]
    def fromRow(row: Row, name: String): Option[Long] =
      if (row.isNull(name)) None else Try(row.getLong(name)).toOption
  }

  implicit object StringIsCassandraPrimitive extends CassandraPrimitive[String] {

    val cassandraType = "text"
    def cls: Class[_] = classOf[java.lang.String]
    def fromRow(row: Row, name: String): Option[String] =
      if (row.isNull(name)) None else Try(row.getString(name)).toOption
  }

  implicit object DoubleIsCassandraPrimitive extends CassandraPrimitive[Double] {

    val cassandraType = "double"
    def cls: Class[_] = classOf[java.lang.Double]
    def fromRow(row: Row, name: String): Option[Double] =
      if (row.isNull(name)) None else Try(row.getDouble(name)).toOption
  }

  implicit object DateIsCassandraPrimitive extends CassandraPrimitive[Date] {

    val cassandraType = "timestamp"
    def cls: Class[_] = classOf[Date]
    def fromRow(row: Row, name: String): Option[Date] =
      if (row.isNull(name)) None else Try(row.getDate(name)).toOption
  }

  implicit object DateTimeisCassandraPrimitive extends CassandraPrimitive[DateTime] {
    val cassandraType = "timestamp"
    def cls: Class[_] = classOf[org.joda.time.DateTime]
    override def toCType(v: org.joda.time.DateTime): AnyRef = v.toDate.asInstanceOf[AnyRef]
    def fromRow(row: Row, name: String): Option[DateTime] =
      if (row.isNull(name)) None else Try(new DateTime(row.getDate(name))).toOption
  }


  implicit object BooleanIsCassandraPrimitive extends CassandraPrimitive[Boolean] {

    val cassandraType = "boolean"
    def cls: Class[_] = classOf[Boolean]
    def fromRow(row: Row, name: String): Option[Boolean] =
      if (row.isNull(name)) None else  Try(row.getBool(name)).toOption
  }

  implicit object UUIDIsCassandraPrimitive extends CassandraPrimitive[UUID] {

    val cassandraType = "uuid"
    def cls: Class[_] = classOf[UUID]
    def fromRow(row: Row, name: String): Option[UUID] =
      if (row.isNull(name)) None else Try(row.getUUID(name)).toOption
  }

  implicit object BigDecimalCassandraPrimitive extends CassandraPrimitive[BigDecimal] {

    val cassandraType = "decimal"
    def cls: Class[_] = classOf[BigDecimal]
    def fromRow(row: Row, name: String): Option[BigDecimal] =
      if (row.isNull(name)) None else Try(BigDecimal(row.getDecimal(name))).toOption
  }

  implicit object InetAddressCassandraPrimitive extends CassandraPrimitive[InetAddress] {

    val cassandraType = "inet"
    def cls: Class[_] = classOf[InetAddress]
    def fromRow(row: Row, name: String): Option[InetAddress] =
      if (row.isNull(name)) None else Try(row.getInet(name)).toOption
  }

  implicit object BigIntCassandraPrimitive extends CassandraPrimitive[BigInt] {

    val cassandraType = "varint"
    def cls: Class[_] = classOf[BigInt]
    def fromRow(row: Row, name: String): Option[BigInt] =
      if (row.isNull(name)) None else Try(BigInt(row.getVarint(name))).toOption
  }

  implicit object BlobIsCassandraPrimitive extends CassandraPrimitive[ByteBuffer] {
    val cassandraType = "blob"
    def cls: Class[_] = classOf[ByteBuffer]

    def fromRow(row: Row, name: String): Option[ByteBuffer] =
      if (row.isNull(name)) None else Try(row.getBytes(name)).toOption
  }
}
