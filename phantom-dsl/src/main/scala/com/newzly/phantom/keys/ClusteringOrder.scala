/*
 * Copyright 2013 newzly ltd.
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

trait Ascending {
  self: ClusteringOrder[_] =>
}

trait Descending {
  self: ClusteringOrder[_] =>
}