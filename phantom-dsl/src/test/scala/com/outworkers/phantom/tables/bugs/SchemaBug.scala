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
package com.outworkers.phantom.tables.bugs

case class Model(
  id: Int,
  quality: Int,
  name: String
)

abstract class Schema extends CassandraTable[Schema, Model] with RootConnector {
  object id extends IntColumn(this) with PartitionKey
  object quality extends IntColumn(this)
  object name extends StringColumn(this)
  object eventId extends LongColumn(this)
}
