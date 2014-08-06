/*
 *
 *  * Copyright 2014 newzly ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.udt

import java.util.UUID

import com.datastax.driver.core.Row

import com.twitter.conversions.time._
import com.twitter.util.Await

import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.BaseTest

/*
class TypeDefinitionTest extends BaseTest {
  val keySpace = "udt_test"

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(TestTable.create.execute(), 2.seconds)
  }

  it should "correctly serialise a UDT definition into a schema" in {
    val address = new Address

    address.schema() shouldEqual "fsa"
  }
}

case class TestRecord(id: UUID, str: String, address: Address)

class TestTable extends CassandraTable[TestTable, TestRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object str extends StringColumn(this)
  object address extends UDT[TestTable, TestRecord, Address](this) {

    val keySpace = "udt_test"
  }

  def fromRow(r: Row): TestRecord = TestRecord(id(r), str(r), address(r))
}

object TestTable extends TestTable

class Address extends UDT[TestTable, TestRecord, Address](TestTable) {

  object id extends UUIDField[TestTable, TestRecord, Address](this)
  object street extends StringField[TestTable, TestRecord, Address](this)

  object postcode extends StringField[TestTable, TestRecord, Address](this)

  val keySpace = "udt_test"
}
*/