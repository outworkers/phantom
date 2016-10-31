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
package com.outworkers.phantom.jdk8

import java.time.{LocalDate, OffsetDateTime}

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.jdk8.dsl._
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

case class OptionalJdk8Row(
  pkey: String,
  offsetDateTime: Option[OffsetDateTime],
  zonedDateTime: Option[ZonedDateTime],
  localDate: Option[LocalDate]
)

sealed class OptionalPrimitivesJdk8 extends CassandraTable[ConcreteOptionalPrimitivesJdk8, OptionalJdk8Row] {

  object pkey extends StringColumn(this) with PartitionKey[String]

  object offsetDateTime extends OptionalOffsetDateTimeColumn(this)

  object zonedDateTime extends OptionalZonedDateTimeColumn(this)

  object localDate extends OptionalJdkLocalDateColumn(this)

  override def fromRow(r: Row): OptionalJdk8Row = {
    OptionalJdk8Row(
      pkey = pkey(r),
      offsetDateTime = offsetDateTime(r),
      zonedDateTime = zonedDateTime(r),
      localDate = localDate(r)
    )
  }
}

abstract class ConcreteOptionalPrimitivesJdk8 extends OptionalPrimitivesJdk8 with RootConnector {

  def store(primitive: OptionalJdk8Row): InsertQuery.Default[ConcreteOptionalPrimitivesJdk8, OptionalJdk8Row] = {
    insert.value(_.pkey, primitive.pkey)
      .value(_.offsetDateTime, primitive.offsetDateTime)
      .value(_.zonedDateTime, primitive.zonedDateTime)
      .value(_.localDate, primitive.localDate)
  }

  override val tableName = "OptionalPrimitivesJdk8"

}