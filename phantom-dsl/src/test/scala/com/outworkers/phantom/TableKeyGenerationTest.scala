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

import com.outworkers.phantom.exceptions.{InvalidClusteringKeyException, InvalidPrimaryKeyException}
import com.outworkers.phantom.tables._
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}

class TableKeyGenerationTest extends FlatSpec with Matchers with ParallelTestExecution {

  it should "correctly create a Compound key from a table with a single Partition key" in {
    TestDatabase.tableWithSingleKey.defineTableKey() shouldEqual s"PRIMARY KEY (id)"
  }

  it should "correctly create a Compound key from a table with a single Partition key and one Primary key" in {
    TestDatabase.tableWithCompoundKey.defineTableKey() shouldEqual s"PRIMARY KEY (id, second)"
  }

  it should "correctly create a Composite key from a table with a two Partition keys and one Primary key" in {
    TestDatabase.tableWithCompositeKey.defineTableKey() shouldEqual s"PRIMARY KEY ((id, second_part), second)"
  }

  it should "throw an error if the schema has no PartitionKey" in {
    intercept[InvalidPrimaryKeyException] {
      TestDatabase.tableWithNoKey.defineTableKey()
    }
  }

  it should "throw an error if the table uses a ClusteringColumn with PrimaryKeys" in {
    intercept[InvalidClusteringKeyException] {
      TestDatabase.brokenClusteringTable.defineTableKey()
    }
  }
}
