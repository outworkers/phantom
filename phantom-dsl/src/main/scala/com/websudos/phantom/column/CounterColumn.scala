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
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.websudos.phantom.{ CassandraPrimitive, CassandraTable }

private[phantom] trait CounterRestriction[T]

class CounterColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record])
  extends Column[Owner, Record, Long](table) with CounterRestriction[Long] {

  val cassandraType = "counter"
  val primitive = CassandraPrimitive[Long]
  override val isCounterColumn = true

  def toCType(values: Long): AnyRef = primitive.toCType(values)

  def optional(r: Row): Option[Long] = {
    primitive.fromRow(r, name)
  }

}
