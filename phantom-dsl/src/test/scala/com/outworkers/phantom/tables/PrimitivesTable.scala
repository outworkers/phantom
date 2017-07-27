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
package com.outworkers.phantom.tables

import com.outworkers.phantom.dsl._

case class PrimitiveRecord(
  pkey: String,
  long: Long,
  boolean: Boolean,
  bDecimal: BigDecimal,
  double: Double,
  float: Float,
  inet: java.net.InetAddress,
  int: Int,
  date: java.util.Date,
  uuid: java.util.UUID,
  bi: BigInt
)

abstract class PrimitivesTable extends Table[PrimitivesTable, PrimitiveRecord] {
  object pkey extends StringColumn with PartitionKey

  object long extends LongColumn

  object boolean extends BooleanColumn

  object bDecimal extends BigDecimalColumn

  object double extends DoubleColumn

  object float extends FloatColumn

  object inet extends InetAddressColumn

  object int extends IntColumn

  object date extends DateColumn

  object uuid extends UUIDColumn

  object bi extends BigIntColumn
}

case class OldPrimitiveRecord(
  pkey: String,
  long: Long,
  boolean: Boolean,
  bDecimal: BigDecimal,
  double: Double,
  float: Float,
  inet: java.net.InetAddress,
  int: Int,
  date: java.util.Date,
  uuid: UUID,
  bi: BigInt,
  timeuuid: UUID,
  localDate: LocalDate,
  stringList: List[String],
  stringSet: Set[String],
  mapCol: Map[String, Int]
)

abstract class OldDslPrimitivesTable extends CassandraTable[
  OldDslPrimitivesTable,
  OldPrimitiveRecord
] with RootConnector {
  object pkey extends StringColumn(this) with PartitionKey

  object long extends LongColumn(this)

  object boolean extends BooleanColumn(this)

  object bDecimal extends BigDecimalColumn(this)

  object double extends DoubleColumn(this)

  object float extends FloatColumn(this)

  object inet extends InetAddressColumn(this)

  object int extends IntColumn(this)

  object date extends DateColumn(this)

  object uuid extends UUIDColumn(this)

  object bi extends BigIntColumn(this)

  object timeuuid extends TimeUUIDColumn(this)

  object ld extends LocalDateColumn(this)

  object stringList extends ListColumn[String](this)

  object stringSet extends SetColumn[String](this)

  object mapCol extends MapColumn[String, Int](this)

}

