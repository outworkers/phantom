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

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.builder.query.CreateQuery
import com.outworkers.phantom.builder.serializers.KeySpaceSerializer
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._

class TestDatabase(override val connector: CassandraConnection) extends Database[TestDatabase](connector) {

  object articles extends Articles with Connector
  object articlesByAuthor extends ArticlesByAuthor with Connector

  object basicTable extends BasicTable with Connector
  object enumTable extends EnumTable with Connector
  object namedEnumTable extends NamedEnumTable with Connector
  object indexedEnumTable extends NamedPartitionEnumTable with Connector

  object clusteringTable extends ClusteringTable with Connector
  object complexClusteringTable extends ComplexClusteringTable with Connector
  object simpleCompoundKeyTable extends SimpleCompoundKeyTable with Connector
  object complexCompoundKeyTable extends ComplexCompoundKeyTable with Connector

  object counterTableTest extends ConcreteCounterTableTest with Connector
  object secondaryCounterTable extends ConcreteSecondaryCounterTable with Connector
  object brokenCounterCounterTable extends ConcreteBrokenCounterTableTest with Connector

  object indexedCollectionsTable extends IndexedCollectionsTable with Connector
  object indexedEntriesTable extends IndexedEntriesTable with Connector
  object jsonTable extends JsonTable with connector.Connector
  object listCollectionTable extends ListCollectionTable with Connector
  object optionalPrimitives extends OptionalPrimitives with Connector
  object primitives extends Primitives with Connector

  object primitivesJoda extends PrimitivesJoda with Connector

  object primitivesCassandra22 extends PrimitivesCassandra22 with Connector
  object optionalPrimitivesCassandra22 extends OptionalPrimitivesCassandra22 with Connector

  object recipes extends Recipes with Connector {
    override def autocreate(space: KeySpace): CreateQuery.Default[Recipes, Recipe] = {
      create.ifNotExists()(space).`with`(comment eqs "This is a test string")
    }
  }

  object secondaryIndexTable extends SecondaryIndexTable with Connector
  object staticTable extends ConcreteStaticTableTest with Connector
  object staticCollectionTable extends StaticCollectionTableTest with Connector

  object tableWithSingleKey extends TableWithSingleKey with Connector
  object tableWithCompoundKey extends TableWithCompoundKey with Connector
  object tableWithCompositeKey extends TableWithCompositeKey with Connector

  object testTable extends TestTable with Connector
  object timeSeriesTable extends TimeSeriesTable with Connector

  object primaryCollectionsTable extends PrimaryCollectionTable with Connector

  object timeSeriesTableWithTtl extends ConcreteTimeSeriesTableWithTTL with Connector
  object timeSeriesTableWithTtl2 extends ConcreteTimeSeriesTableWithTTL2 with Connector
  object multipleKeysTable extends MultipleKeys with Connector
  object timeuuidTable extends TimeUUIDTable with Connector

  object events extends Events with Connector

  object scalaPrimitivesTable extends ScalaTypesMapTable with Connector
  object optionalIndexesTable extends OptionalSecondaryIndexTable with Connector
  object tuple2Table extends ConcreteTupleColumnTable with Connector
  object nestedTupleTable extends ConcreteNestedTupleColumnTable with Connector
  object tupleCollectionsTable extends ConcreteTupleCollectionsTable with Connector

  object tableTypeTuple extends TupleTypeTable with Connector
  object wideTable extends WideTable with Connector
}

object Connector {
  val default: CassandraConnection = connectors.ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
      )
    ).noHeartbeat().keySpace(
      KeySpace("phantom").ifNotExists().`with`(
        replication eqs SimpleStrategy.replication_factor(1)
      )
    )

}

object TestDatabase extends TestDatabase(Connector.default)