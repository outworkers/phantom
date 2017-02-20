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

import java.net.InetAddress
import java.util.Date

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

import scala.concurrent.Future

case class OptionalPrimitive(
  pkey: String,
  string: Option[String],
  long: Option[Long],
  boolean: Option[Boolean],
  bDecimal: Option[BigDecimal],
  double: Option[Double],
  float: Option[Float],
  inet: Option[InetAddress],
  int: Option[Int],
  date: Option[java.util.Date],
  uuid: Option[java.util.UUID],
  timeuuid: Option[java.util.UUID],
  bi: Option[BigInt]
)

object OptionalPrimitive {

  implicit object OptionalPrimitiveSampler extends Sample[OptionalPrimitive] {
    override def sample: OptionalPrimitive = {
      OptionalPrimitive(
        gen[ShortString].value,
        genOpt[String],
        genOpt[Long],
        genOpt[Boolean],
        genOpt[BigDecimal],
        genOpt[Double],
        genOpt[Float],
        genOpt[InetAddress],
        genOpt[Int],
        genOpt[Date],
        genOpt[UUID],
        Some(UUIDs.timeBased),
        genOpt[BigInt]
      )
    }
  }

  def empty: OptionalPrimitive = {
    OptionalPrimitive(
      pkey = gen[String],
      string = None,
      long = None,
      boolean = None,
      bDecimal = None,
      double = None,
      float = None,
      inet = None,
      int = None,
      date = None,
      uuid = None,
      timeuuid = None,
      bi = None
    )
  }
}

abstract class OptionalPrimitives extends CassandraTable[OptionalPrimitives, OptionalPrimitive] with RootConnector {
  object pkey extends StringColumn(this) with PartitionKey

  object string extends OptionalStringColumn(this)

  object long extends OptionalLongColumn(this)

  object boolean extends OptionalBooleanColumn(this)

  object bDecimal extends OptionalBigDecimalColumn(this)

  object double extends OptionalDoubleColumn(this)

  object float extends OptionalFloatColumn(this)

  object inet extends OptionalInetAddressColumn(this)

  object int extends OptionalIntColumn(this)

  object date extends OptionalDateColumn(this)

  object uuid extends OptionalUUIDColumn(this)

  object timeuuid extends OptionalTimeUUIDColumn(this)

  object bi extends OptionalBigIntColumn(this)

  def findByKey(pkey: String): Future[Option[OptionalPrimitive]] = {
    select.where(_.pkey eqs pkey).one()
  }

  def store(row: OptionalPrimitive): InsertQuery.Default[OptionalPrimitives, OptionalPrimitive] = {
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
      .value(_.string, row.string)
      .value(_.timeuuid, row.timeuuid)
  }

  def updateColumns(row: OptionalPrimitive): Future[ResultSet] = {
    update.where(_.pkey eqs row.pkey)
      .modify(_.long setTo row.long)
      .and(_.bDecimal setTo row.bDecimal)
      .and(_.boolean setTo row.boolean)
      .and(_.double setTo row.double)
      .and(_.float setTo row.float)
      .and(_.inet setTo row.inet)
      .and(_.int setTo row.int)
      .and(_.date setTo row.date)
      .and(_.uuid setTo row.uuid)
      .and(_.bi setTo row.bi)
      .and(_.string setTo row.string)
      .and(_.timeuuid setTo row.timeuuid)
      .future()
  }
}
