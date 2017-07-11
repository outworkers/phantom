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
package com.outworkers.phantom.tables

import com.outworkers.phantom.dsl._

case class StaticCollectionSingle(
  id: UUID,
  clusteringId: UUID,
  static: String
)

abstract class StaticTableTest extends Table[
  StaticTableTest,
  StaticCollectionSingle
] {

  object id extends UUIDColumn with PartitionKey
  object clusteringId extends UUIDColumn with ClusteringOrder with Descending
  object staticTest extends StringColumn with StaticColumn
}

case class StaticCollectionRecord(
  id: UUID,
  clustering: UUID,
  list: List[String]
)

abstract class StaticCollectionTable extends Table[
  StaticCollectionTable,
  StaticCollectionRecord
] {
  object id extends UUIDColumn with PartitionKey
  object clustering extends UUIDColumn with ClusteringOrder with Descending
  object staticList extends ListColumn[String] with StaticColumn
}
