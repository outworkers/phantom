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

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

case class StaticCollectionSingle(id: UUID, clusteringId: UUID, static: String)

sealed class StaticTableTest extends CassandraTable[ConcreteStaticTableTest, StaticCollectionSingle] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object clusteringId extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Descending
  object staticTest extends StringColumn(this) with StaticColumn[String]

  def fromRow(row: Row): StaticCollectionSingle = {
    StaticCollectionSingle(
      id(row),
      clusteringId(row),
      staticTest(row)
    )
  }
}

abstract class ConcreteStaticTableTest extends StaticTableTest with RootConnector


case class StaticCollectionRecord(
  id: UUID,
  clustering: UUID,
  list: List[String]
)

sealed class StaticCollectionTableTest extends CassandraTable[ConcreteStaticCollectionTableTest, StaticCollectionRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object clusteringId extends UUIDColumn(this) with PrimaryKey[UUID] with ClusteringOrder[UUID] with Descending
  object staticList extends ListColumn[String](this) with StaticColumn[List[String]]

  def fromRow(row: Row): StaticCollectionRecord = {
    StaticCollectionRecord(
      id = id(row),
      clustering = clusteringId(row),
      list = staticList(row)
    )
  }
}

abstract class ConcreteStaticCollectionTableTest extends StaticCollectionTableTest with RootConnector {
  def store(record: StaticCollectionRecord): InsertQuery.Default[ConcreteStaticCollectionTableTest, StaticCollectionRecord] = {
    insert.value(_.id, record.id)
      .value(_.clusteringId, record.clustering)
      .value(_.staticList, record.list)
  }
}
