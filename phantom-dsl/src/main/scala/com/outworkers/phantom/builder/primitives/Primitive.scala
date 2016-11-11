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

import java.util.Date

import com.datastax.driver.core.{GettableByIndexData, GettableByNameData, GettableData, LocalDate}
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

  /**
    * A way of maintaining compatibility with the underlying Java driver.
    * The driver often type checks records before casting them and to do that
    * it needs the correct Java Class obtained via classOf[] or .getClass in Java.
    *
    * We use this because the appropriate Scala type is often different than the
    * Java equivalent. For instance, we don't want users to deal with [[java.util.List]],
    * even if the Java Driver will attempt to look for one.
    */
  type PrimitiveType

  protected[this] def nullCheck[T](column: String, row: GettableByNameData)(fn: GettableByNameData => T): Try[T] = {
    if (Option(row).isEmpty || row.isNull(column)) {
      Failure(new Exception(s"Column $column is null") with NoStackTrace)
    } else {
      Try(fn(row))
    }
  }

  protected[this] def nullCheck[T](index: Int, row: GettableByIndexData)(fn: GettableByIndexData => T): Try[T] = {
    if (Option(row).isEmpty || row.isNull(index)) {
      Failure(new Exception(s"Column with index $index is null") with NoStackTrace)
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

  def cassandraType: String

  def fromRow(column: String, row: GettableByNameData): Try[RR]

  def fromRow(index: Int, row: GettableByIndexData): Try[RR]

  def fromString(value: String): RR

  def clz: Class[PrimitiveType]

  def extract(obj: PrimitiveType): RR = identity(obj).asInstanceOf[RR]
}

object Primitive {

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * If this does not highlight properly in your IDE, fear not, it works on my machine :)
    * @tparam T The type parameter to materialise a primitive for.
    * @return A concrete instance of a primitive, materialised via implicit blackbox macros.
    */
  implicit def materializer[T]: Primitive[T] = macro PrimitiveMacro.materializer[T]

  /**
    * Convenience method to materialise the context bound and return a reference to it.
    * This is somewhat shorter syntax than using implicitly.
    * @tparam RR The type of the primitive to retrieve.
    * @return A reference to a concrete materialised implementation of a primitive for the given type.
    */
  def apply[RR : Primitive]: Primitive[RR] = implicitly[Primitive[RR]]
}
