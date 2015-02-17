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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.tables

import java.util.UUID

import org.joda.time.DateTime

import com.websudos.phantom.dsl._
import com.websudos.phantom.PhantomCassandraConnector
import com.websudos.util.testing._

case class TimeSeriesRecord(
  id: UUID,
  name: String,
  timestamp: DateTime
)

sealed class TimeSeriesTable extends CassandraTable[TimeSeriesTable, TimeSeriesRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

  def fromRow(row: Row): TimeSeriesRecord = {
    TimeSeriesRecord(
      id(row),
      name(row),
      timestamp(row)
    )
  }
}

object TimeSeriesTable extends TimeSeriesTable with PhantomCassandraConnector {
  val testUUID = gen[UUID]
}
