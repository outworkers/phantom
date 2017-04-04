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

abstract class PrimitivesTable extends CassandraTable[PrimitivesTable, PrimitiveRecord] {
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
