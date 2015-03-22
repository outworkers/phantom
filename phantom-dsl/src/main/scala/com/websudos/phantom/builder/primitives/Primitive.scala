package com.websudos.phantom.builder.primitives

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.CQLSyntax
import com.websudos.phantom.builder.query.CQLQuery

abstract class Primitive[RR] {
  def asCql(value: RR): String

  def cassandraType: String

  def fromRow(column: String, row: Row): RR

  def fromString(value: String): RR

}

trait DefaultPrimitives {

  implicit object StringPrimitive extends Primitive[String] {
    def asCql(value: String): String = CQLQuery.empty.appendSingleQuote(value).queryString

    override def cassandraType: String = CQLSyntax.Types.Text

    override def fromString(value: String): String = value

    override def fromRow(column: String, row: Row): String = row.getString(column)
  }

  implicit object IntPrimitive extends Primitive[Int] {
    def asCql(value: Int): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Int

    override def fromString(value: String): Int = value.toInt

    override def fromRow(column: String, row: Row): Int = row.getInt(column)
  }

  implicit object DoublePrimitive extends Primitive[Double] {
    def asCql(value: Double): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Double

    override def fromString(value: String): Double = value.toDouble

    override def fromRow(column: String, row: Row): Double = row.getDouble(column)
  }

  implicit object LongPrimitive extends Primitive[Long] {
    def asCql(value: Long): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Long

    override def fromString(value: String): Long = value.toLong

    override def fromRow(column: String, row: Row): Long = row.getLong(column)
  }

  implicit object FloatPrimitive extends Primitive[Float] {
    def asCql(value: Float): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.Float

    override def fromString(value: String): Float = value.toFloat

    override def fromRow(column: String, row: Row): Float = row.getFloat(column)
  }

  implicit object UUIDPrimitive extends Primitive[UUID] {
    def asCql(value: UUID): String = value.toString

    override def cassandraType: String = CQLSyntax.Types.UUID

    override def fromString(value: String): UUID = UUID.fromString(value)

    override def fromRow(column: String, row: Row): UUID = row.getUUID(column)
  }

}


object Primitive {
  def apply[RR : Primitive] = implicitly[Primitive[RR]]
}
