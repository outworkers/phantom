package com.websudos.phantom.tables

import com.websudos.phantom.builder.query.Defaults
import com.websudos.phantom.connectors.KeySpaceDef
import com.websudos.phantom.db.DatabaseImpl

class TestDatabase(override val connector: KeySpaceDef) extends DatabaseImpl(connector) {
  object articles extends ConcreteArticles with connector.Connector
  object basicTable extends ConcreteBasicTable with connector.Connector
  object enumTable extends ConcreteEnumTable with connector.Connector
  object namedEnumTable extends ConcreteNamedEnumTable with connector.Connector
  object clusteringTable extends ConcreteClusteringTable with connector.Connector
  object complexClusteringTable extends ConcreteComplexClusteringTable with connector.Connector
  object brokenClusteringTable extends ConcreteBrokenClusteringTable with connector.Connector
  object simpleCompoundKeyTable extends ConcreteSimpleCompoundKeyTable with connector.Connector
  object complexCompoundKeyTable extends ConcreteComplexCompoundKeyTable with connector.Connector

  object byteBufferTable extends ConcreteByteBufferTable with connector.Connector

  object counterTableTest extends ConcreteCounterTableTest with connector.Connector
  object secondaryCounterTable extends ConcreteSecondaryCounterTable with connector.Connector
  object brokenCounterCounterTable extends ConcreteBrokenCounterTableTest with connector.Connector

  object indexedCollectionsTable extends ConcreteIndexedCollectionsTable with connector.Connector
  object jsonTable extends ConcreteJsonTable with connector.Connector
  object listCollectionTable extends ConcreteListCollectionTable with connector.Connector
  object optionalPrimitives extends ConcreteOptionalPrimitives with connector.Connector
  object primitives extends ConcretePrimitives with connector.Connector

  object primitivesJoda extends ConcretePrimitivesJoda with connector.Connector
  object recipes extends ConcreteRecipes with connector.Connector

  object secondaryIndexTable extends  ConcreteSecondaryIndexTable with connector.Connector
  object staticTable extends ConcreteStaticTableTest with connector.Connector
  object staticCollectionTable extends ConcreteStaticCollectionTableTest with connector.Connector

  object tableWithSingleKey extends ConcreteTableWithSingleKey with connector.Connector
  object tableWithCompoundKey extends ConcreteTableWithCompoundKey with connector.Connector
  object tableWithCompositeKey extends ConcreteTableWithCompositeKey with connector.Connector
  object tableWithNoKey extends ConcreteTableWithNoKey with connector.Connector

  object testTable extends ConcreteTestTable with connector.Connector
  object timeSeriesTable extends ConcreteTimeSeriesTable with connector.Connector
  object timeSeriesTableWithTtl extends ConcreteTimeSeriesTableWithTTL with connector.Connector
  object timeSeriesTableWithTtl2 extends ConcreteTimeSeriesTableWithTTL2 with connector.Connector
  object twoKeysTable extends ConcreteTwoKeys with connector.Connector
}

object TestDatabase extends TestDatabase(Defaults)