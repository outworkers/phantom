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

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.util.testing.sample
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

case class Primitive(
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

sealed class Primitives extends CassandraTable[ConcretePrimitives, Primitive] {
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

  override def fromRow(r: Row): Primitive = extract[Primitive](r)
}

abstract class ConcretePrimitives extends Primitives with RootConnector {

  override val tableName = "Primitives"

  def store(row: Primitive): InsertQuery.Default[ConcretePrimitives, Primitive] = {
    insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
  }

}
