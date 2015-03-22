package com.websudos.phantom.builder.primitives

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.CQLSyntax
import com.websudos.phantom.builder.query.CQLQuery
import org.joda.time.DateTime

import scala.util.Try

abstract class Primitive[RR] {

  protected[this] def nullCheck[T](column: String, row: Row)(fn: Row => T): Option[T] = if (row.isNull(column)) None else Try(fn(row)).toOption

  def asCql(value: RR): String

  def cassandraType: String

  def fromRow(column: String, row: Row): Option[RR]

  def fromString(value: String): RR

}

trait DefaultPrimitives {

  implicit object StringPrimitive extends Primitive[String] {
    def asCql(value: String): String = CQLQuery.empty.appendSingleQuote(value).queryString

    override def cassandraType: String = CQLSyntax.Types.Text

    override def fromString(value: String): String = value

    override def fromRow(column: String, row: Row): Option[String] = {
      nullCheck(column, row) {
        r => r.getString(column)
      }
    }
  }

  implicit object IntPrimitive extends Primitive[Int] {
    def asCql(value: Int): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Int

    override def fromString(value: String): Int = value.toInt

    override def fromRow(column: String, row: Row): Option[Int] = nullCheck(column, row) {
      r => r.getInt(column)
    }
  }

  implicit object DoublePrimitive extends Primitive[Double] {
    def asCql(value: Double): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Double

    override def fromString(value: String): Double = value.toDouble

    override def fromRow(column: String, row: Row): Option[Double] = nullCheck(column, row) {
      r => r.getDouble(column)
    }
  }

  implicit object LongPrimitive extends Primitive[Long] {
    def asCql(value: Long): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Long

    override def fromString(value: String): Long = value.toLong

    override def fromRow(column: String, row: Row): Option[Long] = nullCheck(column, row) {
      r => r.getLong(column)
    }
  }

  implicit object FloatPrimitive extends Primitive[Float] {
    def asCql(value: Float): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Float

    override def fromString(value: String): Float = value.toFloat

    override def fromRow(column: String, row: Row): Option[Float] = nullCheck(column, row) {
      r => r.getFloat(column)
    }
  }

  implicit object UUIDPrimitive extends Primitive[UUID] {
    def asCql(value: UUID): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.UUID

    override def fromString(value: String): UUID = UUID.fromString(value)

    override def fromRow(column: String, row: Row): Option[UUID] = nullCheck(column, row) {
      r => r.getUUID(column)
    }
  }

  implicit object DateIsPrimitive extends Primitive[Date] {

    val cassandraType = CQLSyntax.Types.Timestamp

    def fromRow(row: Row, name: String): Option[Date] =
      if (row.isNull(name)) None else Try(row.getDate(name)).toOption

    override def asCql(value: Date): String = value.toString

    override def fromRow(column: String, row: Row): Option[Date] = nullCheck(column, row) {
      r => r.getDate(column)
    }

    override def fromString(value: String): Date = new DateTime(value).toDate
  }

  implicit object DateTimeisPrimitive extends Primitive[DateTime] {
    val cassandraType = CQLSyntax.Types.Timestamp

    override def asCql(v: DateTime): String = v.toDate.toString

    override def fromRow(column: String, row: Row): Option[DateTime] = nullCheck(column, row) {
      r => new DateTime(r.getDate(column))
    }

    override def fromString(value: String): DateTime = new DateTime(value)
  }


  implicit object BooleanIsPrimitive extends Primitive[Boolean] {

    val cassandraType = CQLSyntax.Types.Boolean

    def fromRow(row: Row, name: String): Option[Boolean] =
      if (row.isNull(name)) None else  Try(row.getBool(name)).toOption

    override def asCql(value: Boolean): String = value.toString

    override def fromRow(column: String, row: Row): Option[Boolean] = nullCheck(column, row) {
      r => r.getBool(column)
    }

    override def fromString(value: String): Boolean = ???
  }

  implicit object BigDecimalPrimitive extends Primitive[BigDecimal] {

    val cassandraType = CQLSyntax.Types.Decimal

    override def fromRow(column: String, row: Row): Option[BigDecimal] = nullCheck(column, row) {
      r => r.getDecimal(column)
    }

    override def asCql(value: BigDecimal): String = value.toString()

    override def fromString(value: String): BigDecimal = BigDecimal(value)
  }

  implicit object InetAddressPrimitive extends Primitive[InetAddress] {

    val cassandraType = CQLSyntax.Types.Inet

    override def fromRow(column: String, row: Row): Option[InetAddress] = nullCheck(column, row) {
      r => r.getInet(column)
    }

    override def asCql(value: InetAddress): String = value.toString

    override def fromString(value: String): InetAddress = InetAddress.getByName(value)
  }

  implicit object BigIntPrimitive extends Primitive[BigInt] {

    val cassandraType = CQLSyntax.Types.Varint

    override def fromRow(column: String, row: Row): Option[BigInt] = nullCheck(column, row) {
      r => r.getVarint(column)
    }

    override def asCql(value: BigInt): String = value.toString()

    override def fromString(value: String): BigInt = BigInt(value)
  }

  implicit object BlobIsPrimitive extends Primitive[ByteBuffer] {

    val cassandraType = CQLSyntax.Types.Blob

    override def fromRow(column: String, row: Row): Option[ByteBuffer] = nullCheck(column, row) {
      r => r.getBytes(column)
    }

    override def asCql(value: ByteBuffer): String = value.toString

    override def fromString(value: String): ByteBuffer = ???
  }

}


object Primitive extends DefaultPrimitives {
  def apply[RR : Primitive] = implicitly[Primitive[RR]]
}
