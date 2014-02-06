package com.newzly.phantom.keys

import com.newzly.phantom.column.{ AbstractColumn, TimeSeries }

/**
 * A trait mixable into a Column to allow clustering order.
 * @tparam ValueType The value stored in the column.
 */
trait ClusteringOrder[ValueType] extends Key[ValueType, ClusteringOrder[ValueType]]{
  self: AbstractColumn[ValueType] =>
  override val isSecondaryKey = true
  private[phantom] implicit val timeSeries: TimeSeries[ValueType]
}
