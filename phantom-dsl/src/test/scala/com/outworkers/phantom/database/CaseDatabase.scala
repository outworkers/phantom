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
package com.outworkers.phantom.database

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.tables.{IndexedCollectionsTable, ListCollectionTable, Recipes}

class CaseDatabase(override val connector: CassandraConnection) extends Database[CaseDatabase](connector) {
  object recipes extends Recipes with Connector
  object indexedCollections extends IndexedCollectionsTable with Connector
  object listCollections extends ListCollectionTable with Connector
}
