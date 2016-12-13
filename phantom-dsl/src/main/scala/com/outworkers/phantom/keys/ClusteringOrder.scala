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
package com.outworkers.phantom.keys

import com.outworkers.phantom.column.AbstractColumn

/**
 * A trait mixable into a Column to allow clustering order.
 */
trait ClusteringOrder extends PrimaryKey {
  self: AbstractColumn[_] =>
  override val isClusteringKey = true

}

trait Ascending {
  self: AbstractColumn[_] with ClusteringOrder =>
  override val isAscending = true
}

trait Descending {
  self: AbstractColumn[_] with ClusteringOrder =>
}
