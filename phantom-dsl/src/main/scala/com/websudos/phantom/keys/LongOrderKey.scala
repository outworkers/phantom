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
package com.websudos.phantom.keys

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.LongColumn

/**
 * A key used for partition tokens and token functions.
 * @tparam Owner The owner of the table.
 * @tparam Record The type of the record.
 */
trait LongOrderKey[Owner <: CassandraTable[Owner, Record], Record] {
  this: CassandraTable[Owner, Record] =>

  object orderId extends LongColumn(this) with PrimaryKey[Long]
}
