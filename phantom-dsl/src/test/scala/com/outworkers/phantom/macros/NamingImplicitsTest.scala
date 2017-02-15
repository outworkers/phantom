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
package com.outworkers.phantom.macros

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.database.CaseDatabase
import com.outworkers.phantom.tables.Connector

class NamingImplicitsTest extends PhantomSuite {

  it should "change the casing of tables if the implicit configuration is overridden" in {
    val snakeCaseDb = new CaseDatabase(Connector.default) {
      override implicit val naming = NamingStrategy.SnakeCase.caseInsensitive
    }

    snakeCaseDb.indexedCollections.tableName shouldEqual "indexed_collections"
    snakeCaseDb.listCollections.tableName shouldEqual "list_collections"
  }

  it should "use snake casing and case sensitivity" in {
    val snakeCaseDb = new CaseDatabase(Connector.default) {
      override implicit val naming = NamingStrategy.SnakeCase.caseSensitive
    }

    snakeCaseDb.indexedCollections.tableName shouldEqual "'indexed_collections'"
    snakeCaseDb.listCollections.tableName shouldEqual "'list_collections'"
  }
}
