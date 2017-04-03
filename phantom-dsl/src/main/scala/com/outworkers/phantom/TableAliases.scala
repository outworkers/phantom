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
package com.outworkers.phantom

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.outworkers.phantom.builder.primitives.Primitive
import org.joda.time.{DateTime, LocalDate}

trait TableAliases[T <: CassandraTable[T, R], R] {
  self: CassandraTable[T, R] =>

  class ListColumn[RR]()(
    implicit ev: Primitive[RR],
    ev2: Primitive[List[RR]]
  ) extends com.outworkers.phantom.column.CollectionColumn[T, R, List, RR](instance)

  class SetColumn[RR]()(
    implicit ev: Primitive[RR],
    ev2: Primitive[Set[RR]]
  ) extends com.outworkers.phantom.column.CollectionColumn[T, R, Set, RR](instance)

  class MapColumn[KK, VV] extends com.outworkers.phantom.column.MapColumn[T, R, KK, VV](instance)

  abstract class JsonColumn[RR] extends com.outworkers.phantom.column.JsonColumn[T, R, RR](instance)
  abstract class OptionalJsonColumn[RR] extends com.outworkers.phantom.column.OptionalJsonColumn[T, R, RR](instance)

  class EnumColumn[RR <: Enumeration#Value : Primitive] extends com.outworkers.phantom.column.PrimitiveColumn[T, R, RR](instance)
  class OptionalEnumColumn[RR <: Enumeration#Value : Primitive] extends com.outworkers.phantom.column.OptionalPrimitiveColumn[T, R, RR](instance)

  abstract class JsonSetColumn[RR] extends com.outworkers.phantom.column.JsonSetColumn[T, R, RR](instance)
  abstract class JsonListColumn[RR] extends com.outworkers.phantom.column.JsonListColumn[T, R, RR](instance)
  abstract class JsonMapColumn[KK, VV] extends com.outworkers.phantom.column.JsonMapColumn[T, R, KK, VV](instance)

  class PrimitiveColumn[RR : Primitive] extends com.outworkers.phantom.column.PrimitiveColumn[T, R, RR](instance)
  class TupleColumn[RR : Primitive] extends PrimitiveColumn[RR]
  class CustomColumn[RR : Primitive] extends PrimitiveColumn[RR]
  class Col[RR : Primitive] extends PrimitiveColumn[RR]
  class Column[RR] extends com.outworkers.phantom.column.Column[T, R, RR](instance)

  class OptionalColumn[RR] extends com.outworkers.phantom.column.OptionalColumn[T, R, RR](instance)
  class OptionalPrimitiveColumn[RR] extends com.outworkers.phantom.column.OptionalPrimitiveColumn[T, R, RR](instance)
  class BigDecimalColumn extends PrimitiveColumn[BigDecimal]
  class BlobColumn extends PrimitiveColumn[ByteBuffer]
  class BigIntColumn extends PrimitiveColumn[BigInt]
  class BooleanColumn extends PrimitiveColumn[Boolean]
  class DateColumn extends PrimitiveColumn[Date]
  class DateTimeColumn extends PrimitiveColumn[DateTime]
  class LocalDateColumn extends PrimitiveColumn[LocalDate]
  class DoubleColumn extends PrimitiveColumn[Double]
  class FloatColumn extends PrimitiveColumn[Float]
  class IntColumn extends PrimitiveColumn[Int]
  class SmallIntColumn extends PrimitiveColumn[Short]
  class TinyIntColumn extends PrimitiveColumn[Byte]
  class InetAddressColumn extends PrimitiveColumn[InetAddress]

  class LongColumn extends PrimitiveColumn[Long]
  class StringColumn extends PrimitiveColumn[String]
  class UUIDColumn extends PrimitiveColumn[UUID]
  class CounterColumn extends com.outworkers.phantom.column.CounterColumn[T, R](instance)
  class TimeUUIDColumn extends com.outworkers.phantom.column.TimeUUIDColumn[T, R](instance)

}
