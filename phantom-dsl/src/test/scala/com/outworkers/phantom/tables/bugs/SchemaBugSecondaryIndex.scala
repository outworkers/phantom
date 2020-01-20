/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

abstract class SchemaBugSecondaryIndex extends Table[SchemaBugSecondaryIndex, SecondaryIndexItem] {

  object id extends UUIDColumn with PartitionKey
  object name extends Col[String]
  object ref extends Col[DateTime] with Index
  object date extends Col[DateTime]
}

case class SecondaryIndexItem(
  id: UUID,
  name: String,
  ref: DateTime,
  date: DateTime
)
