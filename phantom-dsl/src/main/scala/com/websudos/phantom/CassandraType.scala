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
package com.websudos.phantom

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.Row
import org.joda.time.DateTime

import scala.util.Try

trait CassandraWrites[T] {

  def toCType(v: T): AnyRef
  def asCql(v: T): String = toCType(v).toString
  def cassandraType: String
}

trait CassandraPrimitive[T] extends CassandraWrites[T] {

  def toCType(v: T): String = v.toString

  def cls: Class[_]

  def fromCType(c: AnyRef): T = c.asInstanceOf[T]
  def fromRow(row: Row, name: String): Option[T]
}

object CassandraPrimitive {

  def apply[T: CassandraPrimitive]: CassandraPrimitive[T] = implicitly[CassandraPrimitive[T]]

  implicit object IntIsCassandraPrimitive extends CassandraPrimitive[Int] {

    val cassandraType = "int"

    def fromRow(row: Row, name: String): Option[Int] =
      if (row.isNull(name)) None else Try(row.getInt(name)).toOption

    override def cls: Class[_] = classOf[Int]
  }

  implicit object FloatIsCassandraPrimitive extends CassandraPrimitive[Float] {

    val cassandraType = "float"

    def fromRow(row: Row, name: String): Option[Float] =
      if (row.isNull(name)) None else Try(row.getFloat(name)).toOption

    override def cls: Class[_] = classOf[Float]
  }

  implicit object LongIsCassandraPrimitive extends CassandraPrimitive[Long] {

    val cassandraType = "bigint"

    def fromRow(row: Row, name: String): Option[Long] =
      if (row.isNull(name)) None else Try(row.getLong(name)).toOption

    override def cls: Class[_] = classOf[Long]
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

    def cls: Class[_] = classOf[java.util.Date]

    def fromRow(row: Row, name: String): Option[Date] =
      if (row.isNull(name)) None else Try(row.getDate(name)).toOption
  }

  implicit object DateTimeisCassandraPrimitive extends CassandraPrimitive[DateTime] {
    val cassandraType = "timestamp"

    def cls: Class[_] = classOf[java.util.Date]

    override def toCType(v: org.joda.time.DateTime): String = v.toDate.toString

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
