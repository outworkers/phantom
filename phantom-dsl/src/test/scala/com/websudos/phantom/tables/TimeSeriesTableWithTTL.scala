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

import java.util.UUID

import org.joda.time.{DateTime, Seconds}

import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector
import com.websudos.util.testing._


sealed class TimeSeriesTableWithTTL extends CassandraTable[TimeSeriesTableWithTTL, TimeSeriesRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

  override def defaultTTL = Some(Seconds.seconds(5))

  def fromRow(row: Row): TimeSeriesRecord = {
    TimeSeriesRecord(
      id(row),
      name(row),
      timestamp(row)
    )
  }
}

object TimeSeriesTableWithTTL extends TimeSeriesTableWithTTL with PhantomCassandraConnector {
  val testUUID = gen[UUID]
}

sealed class TimeSeriesTableWithTTL2 extends CassandraTable[TimeSeriesTableWithTTL2, TimeSeriesRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object timestamp extends DateTimeColumn(this)

  override def defaultTTL = Some(Seconds.seconds(5))

  def fromRow(row: Row): TimeSeriesRecord = {
    TimeSeriesRecord(
      id(row),
      name(row),
      timestamp(row)
    )
  }
}

object TimeSeriesTableWithTTL2 extends TimeSeriesTableWithTTL2 with PhantomCassandraConnector {
  val testUUID = gen[UUID]
}

