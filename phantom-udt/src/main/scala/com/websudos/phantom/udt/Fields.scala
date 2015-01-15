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
package com.websudos.phantom.udt

import java.net.InetAddress
import java.util.{UUID, Date}

import org.joda.time.DateTime

import com.datastax.driver.core.UDTValue
import com.websudos.phantom.CassandraTable

object Fields {

  class BooleanField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T,
    Boolean](column) {

    def apply(row: UDTValue): Option[Boolean] = Some(row.getBool(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setBool(name, value)
  }

  class StringField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, String](column) {
    def apply(row: UDTValue): Option[String] = Some(row.getString(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setString(name, value)
  }

  class InetField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, InetAddress](column) {
    def apply(row: UDTValue): Option[InetAddress] = Some(row.getInet(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInet(name, value)
  }

  class IntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Int](column) {
    def apply(row: UDTValue): Option[Int] = Some(row.getInt(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInt(name, value)
  }

  class DoubleField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Double](column) {
    def apply(row: UDTValue): Option[Double] = Some(row.getDouble(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDouble(name, value)
  }

  class LongField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Long](column) {
    def apply(row: UDTValue): Option[Long] = Some(row.getLong(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setLong(name, value)
  }

  class BigIntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigInt](column) {
    def apply(row: UDTValue): Option[BigInt] = Some(row.getVarint(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setVarint(name, value.bigInteger)
  }

  class BigDecimalField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigDecimal](column) {
    def apply(row: UDTValue): Option[BigDecimal] = Some(row.getDecimal(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDecimal(name, value.bigDecimal)
  }

  class DateField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, Date](column) {
    def apply(row: UDTValue): Option[Date] = Some(row.getDate(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value)
  }

  class DateTimeField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, DateTime](column) {
    def apply(row: UDTValue): Option[DateTime] = Some(new DateTime(row.getDate(name)))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value.toDate)
  }

  /*
  class UDTField[Owner <: UDTColumn[Owner, ], T <: UDTColumn[_]](column: Owner) extends Field[Owner, T](column) {
    def apply(row: Row): DateTime = new DateTime(row.getDate(name))
  }*/

  class UUIDField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, UUID](column) {
    def apply(row: UDTValue): Option[UUID] = Some(row.getUUID(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setUUID(name, value)
  }
}

