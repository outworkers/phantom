/*
 * Copyright 2013 newzly ltd.
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
package com
package newzly
package cassandra
package phantom

import java.util.{ UUID, Date }
import java.net.{InetAddress}

import com.datastax.driver.core.Row

import org.joda.time.DateTime

trait CassandraWrites[T] {

  def toCType(v: T): AnyRef
  def cassandraType: String
}

trait CassandraPrimitive[T] extends CassandraWrites[T] {

  def cls: Class[T] = classOf[T]
  def toCType(v: T): AnyRef = v.asInstanceOf[AnyRef]
  def fromCType(c: AnyRef): T = c.asInstanceOf[T]
  def fromRow(row: Row, name: String): Option[T]
}

object CassandraPrimitive {

  def apply[T: CassandraPrimitive]: CassandraPrimitive[T] = implicitly[CassandraPrimitive[T]]

  implicit object IntIsCassandraPrimitive extends CassandraPrimitive[Int] {

    val cassandraType = "int"
    def fromRow(row: Row, name: String): Option[Int] = Option(row.getInt(name))
  }

  implicit object FloatIsCassandraPrimitive extends CassandraPrimitive[Float] {

    val cassandraType = "float"
    def fromRow(row: Row, name: String): Option[Float] = Option(row.getFloat(name))
  }

  implicit object LongIsCassandraPrimitive extends CassandraPrimitive[Long] {

    val cassandraType = "bigint"
    def fromRow(row: Row, name: String): Option[Long] = Option(row.getLong(name))
  }

  implicit object StringIsCassandraPrimitive extends CassandraPrimitive[String] {

    val cassandraType = "text"
    def fromRow(row: Row, name: String): Option[String] = Option(row.getString(name))
  }

  implicit object DoubleIsCassandraPrimitive extends CassandraPrimitive[Double] {

    val cassandraType = "double"
    def fromRow(row: Row, name: String): Option[Double] = Option(row.getDouble(name))
  }

  implicit object DateIsCassandraPrimitive extends CassandraPrimitive[Date] {

    val cassandraType = "timestamp"
    def fromRow(row: Row, name: String): Option[Date] = Option(row.getDate(name))
  }

  implicit object DateTimeisCassandraPrimitive extends CassandraPrimitive[DateTime] {
    val cassandraType = "timestamp"
    def fromRow(row: Row, name: String): Option[DateTime] = Option(new DateTime(row.getDate(name)))
  }

  implicit object BooleanIsCassandraPrimitive extends CassandraPrimitive[Boolean] {

    val cassandraType = "boolean"
    def fromRow(row: Row, name: String): Option[Boolean] = Option(row.getBool(name))
  }

  implicit object UUIDIsCassandraPrimitive extends CassandraPrimitive[UUID] {

    val cassandraType = "uuid"
    def fromRow(row: Row, name: String): Option[UUID] = Option(row.getUUID(name))
  }

  implicit object BigDecimalCassandraPrimitive extends CassandraPrimitive[BigDecimal] {

    val cassandraType = "decimal"
    def fromRow(row: Row, name: String): Option[BigDecimal] = Option(row.getDecimal(name))
  }

  implicit object InetAddressCassandraPrimitive extends CassandraPrimitive[InetAddress] {

    val cassandraType = "inet"
    def fromRow(row: Row, name: String): Option[InetAddress] = Option(row.getInet(name))
  }

  implicit object BigIntCassandraPrimitive extends CassandraPrimitive[BigInt] {

    val cassandraType = "varint"
    def fromRow(row: Row, name: String): Option[BigInt] = Option(row.getVarint(name))
  }

}