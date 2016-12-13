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
package com.outworkers.phantom

import com.outworkers.phantom.tables._
import org.scalatest.{FlatSpec, Matchers}

class BasicTableMethods extends FlatSpec with Matchers {

  it should "retrieve the correct number of columns in a simple table" in {
    TestDatabase.basicTable.columns.size shouldEqual 4
  }

  it should "retrieve the correct number of columns in a big table" in {
    TestDatabase.complexCompoundKeyTable.columns.size shouldEqual 10
  }

  it should "retrieve the correct number of primary keys for a table" in {
    TestDatabase.simpleCompoundKeyTable.primaryKeys.size shouldEqual 2
    TestDatabase.simpleCompoundKeyTable.partitionKeys.size shouldEqual 1
  }

  it should "retrieve the correct number of clustering keys for a table" in {
    TestDatabase.clusteringTable.clusteringColumns.size shouldEqual 2
  }
}
