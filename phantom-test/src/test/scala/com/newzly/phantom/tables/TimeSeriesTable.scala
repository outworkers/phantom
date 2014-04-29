package com.newzly.phantom.tables

import java.util.UUID
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.{ ModelSampler, TestSampler }
import com.newzly.util.testing.Sampler

case class TimeSeriesRecord(
  id: UUID,
  name: String,
  timestamp: DateTime
)

object TimeSeriesRecord extends ModelSampler[TimeSeriesRecord] {
  def sample: TimeSeriesRecord = {
    TimeSeriesRecord(
      UUIDs.timeBased(),
      Sampler.getARandomString,
      new DateTime()
    )
  }
}

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

object TimeSeriesTable extends TimeSeriesTable with TestSampler[TimeSeriesTable, TimeSeriesRecord]
