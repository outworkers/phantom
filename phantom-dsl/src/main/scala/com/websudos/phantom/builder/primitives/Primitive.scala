package com.websudos.phantom.builder.primitives

import java.util.UUID

import com.websudos.phantom.builder.query.CQLQuery

abstract class Primitive[RR] {
  def asCql(value: RR): String
}

trait DefaultPrimitives {

  implicit object StringPrimitive extends Primitive[String] {
    def asCql(value: String): String = CQLQuery.empty.appendSingleQuote(value).queryString
  }

  implicit object IntPrimitive extends Primitive[Int] {
    def asCql(value: Int): String = value.toString
  }

  implicit object DoublePrimitive extends Primitive[Double] {
    def asCql(value: Double): String = value.toString
  }

  implicit object LongPrimitive extends Primitive[Long] {
    def asCql(value: Long): String = value.toString
  }

  implicit object FloatPrimitive extends Primitive[Float] {
    def asCql(value: Float): String = value.toString
  }

  implicit object UUIDPrimitive extends Primitive[UUID] {
    def asCql(value: UUID): String = value.toString
  }

}
