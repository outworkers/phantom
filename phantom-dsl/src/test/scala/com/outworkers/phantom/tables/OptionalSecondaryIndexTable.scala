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
package com.outworkers.phantom.tables

import java.util.UUID

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class OptionalSecondaryRecord(
  id: UUID,
  secondary: Option[Int]
)

sealed class OptionalSecondaryIndexTable extends
  CassandraTable[ConcreteOptionalSecondaryIndexTable, OptionalSecondaryRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object secondary extends OptionalIntColumn(this) with Index[Option[Int]]

  def fromRow(row: Row): OptionalSecondaryRecord = {
    OptionalSecondaryRecord(
      id(row),
      secondary(row)
    )
  }
}

abstract class ConcreteOptionalSecondaryIndexTable
  extends OptionalSecondaryIndexTable with RootConnector {

  def store(rec: OptionalSecondaryRecord): Future[ResultSet] = {
    insert.value(_.id, rec.id)
      .value(_.secondary, rec.secondary)
      .future()
  }

  def findById(id: UUID): Future[Option[OptionalSecondaryRecord]] = {
    select.where(_.id eqs id).one()
  }

  def findByOptionalSecondary(sec: Int): Future[Option[OptionalSecondaryRecord]] = {
    select.where(_.secondary eqs sec).one()
  }

}


