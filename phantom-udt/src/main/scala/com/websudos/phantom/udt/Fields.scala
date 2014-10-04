package com.websudos.phantom.udt

import java.net.InetAddress
import java.util.{UUID, Date}

import org.joda.time.DateTime

import com.datastax.driver.core.UDTValue
import com.websudos.phantom.CassandraTable

object Fields {

  class BooleanField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T,
    Boolean](column) {

    def apply(row: UDTValue): Option[Boolean] = Option(row.getBool(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setBool(name, value)
  }

  class StringField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, String](column) {
    def apply(row: UDTValue): Option[String] = Option(row.getString(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setString(name, value)
  }

  class InetField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, InetAddress](column) {
    def apply(row: UDTValue): Option[InetAddress] = Option(row.getInet(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInet(name, value)
  }

  class IntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Int](column) {
    def apply(row: UDTValue): Option[Int] = Option(row.getInt(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInt(name, value)
  }

  class DoubleField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Double](column) {
    def apply(row: UDTValue): Option[Double] = Option(row.getDouble(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDouble(name, value)
  }

  class LongField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Long](column) {
    def apply(row: UDTValue): Option[Long] = Option(row.getLong(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setLong(name, value)
  }

  class BigIntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigInt](column) {
    def apply(row: UDTValue): Option[BigInt] = Option(row.getVarint(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setVarint(name, value.bigInteger)
  }

  class BigDecimalField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigDecimal](column) {
    def apply(row: UDTValue): Option[BigDecimal] = Option(row.getDecimal(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDecimal(name, value.bigDecimal)
  }

  class DateField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, Date](column) {
    def apply(row: UDTValue): Option[Date] = Option(row.getDate(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value)
  }

  class DateTimeField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, DateTime](column) {
    def apply(row: UDTValue): Option[DateTime] = Option(new DateTime(row.getDate(name)))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value.toDate)
  }

  /*
  class UDTField[Owner <: UDTColumn[Owner, ], T <: UDTColumn[_]](column: Owner) extends Field[Owner, T](column) {
    def apply(row: Row): DateTime = new DateTime(row.getDate(name))
  }*/

  class UUIDField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, UUID](column) {
    def apply(row: UDTValue): Option[UUID] = Option(row.getUUID(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setUUID(name, value)
  }
}

