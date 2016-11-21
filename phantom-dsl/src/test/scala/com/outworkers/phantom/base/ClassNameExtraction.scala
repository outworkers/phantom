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
package com.outworkers.phantom.base

import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.phantom.dsl._

case class CustomRecord(name: String, mp: Map[String, String])

trait TestTableNames extends CassandraTable[TestTableNames, CustomRecord] {
  object rec extends StringColumn(this) with PartitionKey
  object sampleLongTextColumnDefinition extends MapColumn[String, String](this)

  override def fromRow(r: Row): CustomRecord = {
    CustomRecord(
      rec(r),
      sampleLongTextColumnDefinition(r)
    )
  }
}

object TestTableNames extends TestTableNames

object Test extends PrimitiveColumn[TestTableNames, CustomRecord, String](TestTableNames)

trait TestNames extends TestTableNames

class Parent extends TestNames
class Parent2 extends Parent

class ClassNameExtraction extends FlatSpec with Matchers {


  it should "correctly name objects inside record classes " in {
    TestTableNames.rec.name shouldEqual "rec"
  }

  it should "correctly extract long object name definitions in nested record classes" in {
    TestTableNames.sampleLongTextColumnDefinition.name shouldEqual "sampleLongTextColumnDefinition"
  }

  it should "correctly name Cassandra Tables" in {
    TestTableNames.tableName shouldEqual "testTableNames"
  }

  it should "correctly extract the object name " in {
    Test.name shouldEqual "Test"
  }
}
