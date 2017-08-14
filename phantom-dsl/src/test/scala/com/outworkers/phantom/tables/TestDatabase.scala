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

import com.datastax.driver.core.SocketOptions
import com.outworkers.phantom.builder.query.CreateQuery
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.bugs.{JsonPreparedTable, SchemaBug656Table, SchemaBug663Table}
import com.outworkers.phantom.tables.sasi.{MultiSASITable, SASIIndexedArticles}

class TestDatabase(
  override val connector: CassandraConnection
) extends Database[TestDatabase](connector) {

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

  object counterTableTest extends CounterTableTest with Connector
  object secondaryCounterTable extends SecondaryCounterTable with Connector
  object brokenCounterCounterTable extends BrokenCounterTableTest with Connector

  object indexedCollectionsTable extends IndexedCollectionsTable with Connector
  object indexedEntriesTable extends IndexedEntriesTable with Connector
  object jsonTable extends JsonTable with Connector
  object listCollectionTable extends ListCollectionTable with Connector
  object optionalPrimitives extends OptionalPrimitives with Connector
  object primitives extends PrimitivesTable with Connector
  object oldPrimitives extends OldDslPrimitivesTable with Connector

  object primitivesJoda extends PrimitivesJoda with Connector

  object primitivesCassandra22 extends PrimitivesCassandra22 with Connector
  object optionalPrimitivesCassandra22 extends OptionalPrimitivesCassandra22 with Connector

  object recipes extends Recipes with Connector {
    override def autocreate(space: KeySpace): CreateQuery.Default[Recipes, Recipe] = {
      create.ifNotExists()(space).`with`(comment eqs "This is a test string")
    }
  }

  object secondaryIndexTable extends SecondaryIndexTable with Connector
  object staticTable extends StaticTableTest with Connector
  object staticCollectionTable extends StaticCollectionTable with Connector

  object tableWithSingleKey extends TableWithSingleKey with Connector
  object tableWithCompoundKey extends TableWithCompoundKey with Connector
  object tableWithCompositeKey extends TableWithCompositeKey with Connector

  object testTable extends TestTable with Connector
  object timeSeriesTable extends TimeSeriesTable with Connector

  object primaryCollectionsTable extends PrimaryCollectionTable with Connector

  object timeSeriesTableWithTtl extends TimeSeriesTableWithTTL with Connector
  object timeSeriesTableWithTtl2 extends TimeSeriesTableWithTTL2 with Connector
  object multipleKeysTable extends MultipleKeys with Connector
  object timeuuidTable extends TimeUUIDTable with Connector

  object events extends Events with Connector
  object nestedCollectionTable extends NestedCollectionTable with Connector
  object scalaPrimitivesTable extends ScalaTypesMapTable with Connector
  object optionalIndexesTable extends OptionalSecondaryIndexTable with Connector
  object tuple2Table extends TupleColumnTable with Connector
  object nestedTupleTable extends NestedTupleColumnTable with Connector
  object tupleCollectionsTable extends TupleCollectionsTable with Connector

  object tableTypeTuple extends TupleTypeTable with Connector
  object wideTable extends WideTable with Connector
  object sessionsByUser extends SessionsByUserId with Connector

  object optDerivedTable extends OptionalDerivedTable with Connector
  object schemaBug656 extends SchemaBug656Table with Connector
  object schemaBug663Table extends SchemaBug663Table with Connector

  object derivedPrimitivesTable extends DerivedPrimitivesTable with Connector
  object sasiIndexedArticles extends SASIIndexedArticles with Connector
  object multiSasiTable extends MultiSASITable with Connector
  object jsonPreparedTable extends JsonPreparedTable with Connector
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

object TestDatabase extends TestDatabase(Connector.default) {
  {
    final class $anon extends _root_.com.outworkers.phantom.macros.DatabaseHelper[com.outworkers.phantom.tables.TestDatabase] {
      def tables(db: com.outworkers.phantom.tables.TestDatabase): _root_.scala.collection.immutable.Seq[(_root_.com.outworkers.phantom.CassandraTable[_$2, _$3] forSome {
        type _$2;
        type _$3
      })] = _root_.scala.collection.immutable.Seq.apply[(_root_.com.outworkers.phantom.CassandraTable[_$2, _$3] forSome {
        type _$2;
        type _$3
      })](db.articles, db.articlesByAuthor, db.basicTable, db.enumTable, db.namedEnumTable, db.indexedEnumTable, db.clusteringTable, db.complexClusteringTable, db.simpleCompoundKeyTable, db.complexCompoundKeyTable, db.counterTableTest, db.secondaryCounterTable, db.brokenCounterCounterTable, db.indexedCollectionsTable, db.indexedEntriesTable, db.jsonTable, db.listCollectionTable, db.optionalPrimitives, db.primitives, db.oldPrimitives, db.primitivesJoda, db.primitivesCassandra22, db.optionalPrimitivesCassandra22, db.recipes, db.secondaryIndexTable, db.staticTable, db.staticCollectionTable, db.tableWithSingleKey, db.tableWithCompoundKey, db.tableWithCompositeKey, db.testTable, db.timeSeriesTable, db.primaryCollectionsTable, db.timeSeriesTableWithTtl, db.timeSeriesTableWithTtl2, db.multipleKeysTable, db.timeuuidTable, db.events, db.nestedCollectionTable, db.scalaPrimitivesTable, db.optionalIndexesTable, db.tuple2Table, db.nestedTupleTable, db.tupleCollectionsTable, db.tableTypeTuple, db.wideTable, db.sessionsByUser, db.optDerivedTable, db.schemaBug656, db.schemaBug663Table, db.derivedPrimitivesTable, db.sasiIndexedArticles, db.multiSasiTable, db.jsonPreparedTable);
      def createQueries(db: com.outworkers.phantom.tables.TestDatabase)(implicit space: _root_.com.outworkers.phantom.connectors.KeySpace): _root_.com.outworkers.phantom.database.QueryCollection[_root_.scala.collection.Seq] = new _root_.com.outworkers.phantom.database.QueryCollection[_root_.scala.collection.Seq](_root_.scala.collection.Seq.apply(db.articles.autocreate(space), db.articlesByAuthor.autocreate(space), db.basicTable.autocreate(space), db.enumTable.autocreate(space), db.namedEnumTable.autocreate(space), db.indexedEnumTable.autocreate(space), db.clusteringTable.autocreate(space), db.complexClusteringTable.autocreate(space), db.simpleCompoundKeyTable.autocreate(space), db.complexCompoundKeyTable.autocreate(space), db.counterTableTest.autocreate(space), db.secondaryCounterTable.autocreate(space), db.brokenCounterCounterTable.autocreate(space), db.indexedCollectionsTable.autocreate(space), db.indexedEntriesTable.autocreate(space), db.jsonTable.autocreate(space), db.listCollectionTable.autocreate(space), db.optionalPrimitives.autocreate(space), db.primitives.autocreate(space), db.oldPrimitives.autocreate(space), db.primitivesJoda.autocreate(space), db.primitivesCassandra22.autocreate(space), db.optionalPrimitivesCassandra22.autocreate(space), db.recipes.autocreate(space), db.secondaryIndexTable.autocreate(space), db.staticTable.autocreate(space), db.staticCollectionTable.autocreate(space), db.tableWithSingleKey.autocreate(space), db.tableWithCompoundKey.autocreate(space), db.tableWithCompositeKey.autocreate(space), db.testTable.autocreate(space), db.timeSeriesTable.autocreate(space), db.primaryCollectionsTable.autocreate(space), db.timeSeriesTableWithTtl.autocreate(space), db.timeSeriesTableWithTtl2.autocreate(space), db.multipleKeysTable.autocreate(space), db.timeuuidTable.autocreate(space), db.events.autocreate(space), db.nestedCollectionTable.autocreate(space), db.scalaPrimitivesTable.autocreate(space), db.optionalIndexesTable.autocreate(space), db.tuple2Table.autocreate(space), db.nestedTupleTable.autocreate(space), db.tupleCollectionsTable.autocreate(space), db.tableTypeTuple.autocreate(space), db.wideTable.autocreate(space), db.sessionsByUser.autocreate(space), db.optDerivedTable.autocreate(space), db.schemaBug656.autocreate(space), db.schemaBug663Table.autocreate(space), db.derivedPrimitivesTable.autocreate(space), db.sasiIndexedArticles.autocreate(space), db.multiSasiTable.autocreate(space), db.jsonPreparedTable.autocreate(space)))
    };
    new $anon()
  }
}
