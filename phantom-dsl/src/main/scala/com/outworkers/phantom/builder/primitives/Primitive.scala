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

import java.nio.ByteBuffer
import java.util.Date

import com.datastax.driver.core.exceptions.InvalidTypeException
import com.datastax.driver.core.{LocalDate, ProtocolVersion}
import com.outworkers.phantom.Row
import com.outworkers.phantom.builder.QueryBuilder
import org.joda.time.DateTime

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
    if (Option(row).isEmpty || row.isNull(index)) {
      Failure(new Exception(s"Column $index is null") with NoStackTrace)
    } else {
      Try(fn(row))
    }
  }

  protected[this] def nullCheck[T](
    column: String,
    row: Row
  )(fn: Row => T): Try[T] = {
    if (Option(row).isEmpty || row.isNull(column)) {
      Failure(new Exception(s"Column $column is null") with NoStackTrace)
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

  val nullValue = None.orNull

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
