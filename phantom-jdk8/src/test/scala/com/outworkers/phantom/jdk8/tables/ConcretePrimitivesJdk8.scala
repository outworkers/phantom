/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.outworkers.phantom.jdk8.tables

import java.time.{LocalDate, OffsetDateTime}

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._
import com.outworkers.phantom.jdk8.dsl._

case class Jdk8Row(
  pkey: String,
  offsetDateTime: OffsetDateTime,
  zonedDateTime: ZonedDateTime,
  localDate: LocalDate
)

sealed class PrimitivesJdk8 extends CassandraTable[ConcretePrimitivesJdk8, Jdk8Row] {

  object pkey extends StringColumn(this) with PartitionKey[String]

  object offsetDateTime extends OffsetDateTimeColumn(this)

  object zonedDateTime extends ZonedDateTimeColumn(this)

  object localDate extends JdkLocalDateColumn(this)

  override def fromRow(r: Row): Jdk8Row = {
    Jdk8Row(
      pkey = pkey(r),
      offsetDateTime = offsetDateTime(r),
      zonedDateTime = zonedDateTime(r),
      localDate = localDate(r)
    )
  }
}

abstract class ConcretePrimitivesJdk8 extends PrimitivesJdk8 with RootConnector {

  def store(primitive: Jdk8Row): InsertQuery.Default[ConcretePrimitivesJdk8, Jdk8Row] = {
    insert.value(_.pkey, primitive.pkey)
      .value(_.offsetDateTime, primitive.offsetDateTime)
      .value(_.zonedDateTime, primitive.zonedDateTime)
      .value(_.localDate, primitive.localDate)
  }

  override val tableName = "PrimitivesJdk8"

}