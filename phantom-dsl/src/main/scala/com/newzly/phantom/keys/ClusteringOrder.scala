package com.newzly.phantom.keys

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.{TimeSeries, Column}

/**
 * A trait mixable into a Column to allow clustering order.
 * @tparam Owner The owner of the record.
 * @tparam Record The case class record to store.
 */
trait ClusteringOrder[Owner <: CassandraTable[Owner, Record], Record, ValueType] {

  self: Column[Owner, Record, ValueType] =>

  override val isKey = true

  implicit def timeSeries: TimeSeries[ValueType]
}
