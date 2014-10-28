/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.tables

import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector

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

sealed class Primitives extends CassandraTable[Primitives, Primitive] {
  override def fromRow(r: Row): Primitive = {
    Primitive(
      pkey(r),
      long(r),
      boolean(r),
      bDecimal(r),
      double(r),
      float(r),
      inet(r),
      int(r),
      date(r),
      uuid(r),
      bi(r)
    )
  }

  object pkey extends StringColumn(this) with PartitionKey[String]

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
}

object Primitives extends Primitives with PhantomCassandraConnector {
  override val tableName = "Primitives"

}
