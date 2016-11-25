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

import java.util.UUID

import com.datastax.driver.core.{PoolingOptions, SocketOptions}
import com.outworkers.phantom.connectors
import com.outworkers.phantom.builder.query.CreateQuery
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._

class TestDatabase(override val connector: CassandraConnection) extends Database[TestDatabase](connector) {
  object articles extends ConcreteArticles with connector.Connector
  object articlesByAuthor extends ConcreteArticlesByAuthor with connector.Connector

  object basicTable extends ConcreteBasicTable with connector.Connector
  object enumTable extends ConcreteEnumTable with connector.Connector
  object namedEnumTable extends ConcreteNamedEnumTable with connector.Connector
  object indexedEnumTable extends ConcreteNamedPartitionEnumTable with connector.Connector

  object clusteringTable extends ConcreteClusteringTable with connector.Connector
  object complexClusteringTable extends ConcreteComplexClusteringTable with connector.Connector
  object brokenClusteringTable extends ConcreteBrokenClusteringTable with connector.Connector
  object simpleCompoundKeyTable extends ConcreteSimpleCompoundKeyTable with connector.Connector
  object complexCompoundKeyTable extends ConcreteComplexCompoundKeyTable with connector.Connector

  object counterTableTest extends ConcreteCounterTableTest with connector.Connector
  object secondaryCounterTable extends ConcreteSecondaryCounterTable with connector.Connector
  object brokenCounterCounterTable extends ConcreteBrokenCounterTableTest with connector.Connector

  object indexedCollectionsTable extends ConcreteIndexedCollectionsTable with connector.Connector
  object indexedEntriesTable extends ConcreteIndexedEntriesTable with connector.Connector
  object jsonTable extends ConcreteJsonTable with connector.Connector
  object listCollectionTable extends ConcreteListCollectionTable with connector.Connector
  object optionalPrimitives extends ConcreteOptionalPrimitives with connector.Connector
  object primitives extends ConcretePrimitives with connector.Connector

  object primitivesJoda extends ConcretePrimitivesJoda with connector.Connector

  object primitivesCassandra22 extends ConcretePrimitivesCassandra22 with connector.Connector
  object optionalPrimitivesCassandra22 extends ConcreteOptionalPrimitivesCassandra22 with connector.Connector

  object recipes extends ConcreteRecipes with connector.Connector {
    override def autocreate(space: KeySpace): CreateQuery.Default[ConcreteRecipes, Recipe] = {
      create.ifNotExists()(space).`with`(comment eqs "This is a test string")
    }
  }

  object secondaryIndexTable extends ConcreteSecondaryIndexTable with connector.Connector
  object staticTable extends ConcreteStaticTableTest with connector.Connector
  object staticCollectionTable extends ConcreteStaticCollectionTableTest with connector.Connector

  object tableWithSingleKey extends ConcreteTableWithSingleKey with connector.Connector
  object tableWithCompoundKey extends ConcreteTableWithCompoundKey with connector.Connector
  object tableWithCompositeKey extends ConcreteTableWithCompositeKey with connector.Connector
  object tableWithNoKey extends ConcreteTableWithNoKey with connector.Connector

  object testTable extends ConcreteTestTable with connector.Connector
  object timeSeriesTable extends ConcreteTimeSeriesTable with connector.Connector {
    val testUUID = UUID.randomUUID()
  }

  object primaryCollectionsTable extends ConcretePrimaryCollectionTable with connector.Connector

  object timeSeriesTableWithTtl extends ConcreteTimeSeriesTableWithTTL with connector.Connector
  object timeSeriesTableWithTtl2 extends ConcreteTimeSeriesTableWithTTL2 with connector.Connector
  object multipleKeysTable$ extends ConcreteMultipleKeys with connector.Connector
  object timeuuidTable extends ConcreteTimeUUIDTable with connector.Connector

  object events extends ConcreteEvents with connector.Connector

  object scalaPrimitivesTable extends ConcreteScalaTypesMapTable with connector.Connector
  object optionalIndexesTable extends ConcreteOptionalSecondaryIndexTable with connector.Connector
  object tuple2Table extends ConcreteTupleColumnTable with connector.Connector
  object nestedTupleTable extends ConcreteNestedTupleColumnTable with connector.Connector
  object tupleCollectionsTable extends ConcreteTupleCollectionsTable with connector.Connector
}

object Connector {
  val default = connectors.ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
      )
    ).noHeartbeat().keySpace("phantom")

}

object TestDatabase extends TestDatabase(Connector.default)