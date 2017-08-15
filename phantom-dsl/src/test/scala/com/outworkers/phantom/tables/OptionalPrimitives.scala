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
  uuid: Option[UUID],
  timeuuid: Option[UUID],
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

abstract class OptionalPrimitives extends Table[
  OptionalPrimitives,
  OptionalPrimitive
] {
  object pkey extends StringColumn with PartitionKey

  object string extends OptionalStringColumn

  object long extends OptionalLongColumn

  object boolean extends OptionalBooleanColumn

  object bDecimal extends OptionalBigDecimalColumn

  object double extends OptionalDoubleColumn

  object float extends OptionalFloatColumn

  object inet extends OptionalInetAddressColumn

  object int extends OptionalIntColumn

  object date extends OptionalDateColumn

  object uuid extends OptionalUUIDColumn

  object timeuuid extends OptionalTimeUUIDColumn

  object bi extends OptionalBigIntColumn

  def findByKey(pkey: String): Future[Option[OptionalPrimitive]] = {
    select.where(_.pkey eqs pkey).one()
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
