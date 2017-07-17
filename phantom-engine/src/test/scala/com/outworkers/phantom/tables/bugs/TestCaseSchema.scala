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

import com.outworkers.phantom.dsl._

case class TestCaseContext(
  epicId: Int,
  userStoryId: Int,
  name: String
)

abstract class TestCaseSchema extends Table[
  TestCaseSchema,
  TestCaseContext
] {
  object tenantId extends IntColumn with PartitionKey
  object productId extends IntColumn with PartitionKey
  object epicId extends IntColumn with PrimaryKey
  object userStoryId extends IntColumn with PrimaryKey
  object name extends StringColumn
  //object eventId extends LongColumn
}
