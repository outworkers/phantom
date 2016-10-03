/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.{PoolingOptions, SocketOptions}
import com.websudos.phantom.builder.query.CreateQuery
import com.websudos.phantom.connectors.{ContactPoint, KeySpaceDef}
import com.websudos.phantom.database.DatabaseImpl
import com.websudos.phantom.dsl._

class TestDatabase(override val connector: KeySpaceDef) extends DatabaseImpl(connector) {
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
  object timeSeriesTableWithTtl extends ConcreteTimeSeriesTableWithTTL with connector.Connector
  object timeSeriesTableWithTtl2 extends ConcreteTimeSeriesTableWithTTL2 with connector.Connector
  object multipleKeysTable$ extends ConcreteMultipleKeys with connector.Connector
  object timeuuidTable extends ConcreteTimeUUIDTable with connector.Connector

  object events extends ConcreteEvents with connector.Connector

  object scalaPrimitivesTable extends ConcreteScalaTypesMapTable with connector.Connector
  object optionalIndexesTable extends ConcreteOptionalSecondaryIndexTable with connector.Connector
}

object Connector {
  val default = ContactPoint.local
    .withClusterBuilder(_.withSocketOptions(
      new SocketOptions()
        .setConnectTimeoutMillis(20000)
        .setReadTimeoutMillis(20000)
      )
    )
    .noHeartbeat().keySpace("phantom")

}

object TestDatabase extends TestDatabase(Connector.default)