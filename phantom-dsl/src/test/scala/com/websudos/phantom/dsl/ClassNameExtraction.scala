/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl

import org.scalatest.{ FlatSpec, Matchers }
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._

trait Test {
  private[this] lazy val _name: String = {
    val packagePath = getClass.getName.split("\\.")
    val fullName = packagePath(packagePath.length - 1)


    val index = fullName.indexOf("$$anonfun")

    if (index != -1) {
      val str = fullName.substring(index + 9, fullName.length)
      str.replaceAll("[(\\$\\d+\\$)]", "")
    } else {
      fullName.replaceAll("[(\\$\\d+\\$)]", "")
    }
  }

  def name: String = _name
}


case class CustomRecord(name: String, mp: Map[String, String])

class TestTableNames extends CassandraTable[TestTableNames, CustomRecord] {
  object record extends StringColumn(this) with PartitionKey[String]
  object sampleLongTextColumnDefinition extends MapColumn[TestTableNames, CustomRecord, String, String](this)

  override def fromRow(r: Row): CustomRecord = {
    CustomRecord(record(r), sampleLongTextColumnDefinition(r))
  }
}

object TestTableNames extends TestTableNames

object Test extends PrimitiveColumn[TestTableNames, CustomRecord, String](TestTableNames)


class TestNames {

  private[this] lazy val _name: String = {
    val packagePath = getClass.getName.split("\\.")
    val fullName = packagePath(packagePath.length - 1)

    val index = fullName.indexOf("$$anonfun")
    val str = fullName.substring(index + 9, fullName.length)
    str.replaceAll("(\\$\\d+\\$)", "")
  }
  def name: String = _name
}

class Parent extends TestNames
class Parent2 extends Parent

class ClassNameExtraction extends FlatSpec with Matchers {

  it should "get rid of extra naming inside the object" in {
    val test = "$$anonfun23primitives3key$"
    val res = test.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)", "")
    res shouldEqual "key"
  }

  it should "correctly name objects inside record classes " in {
    TestTableNames.record.name shouldEqual "record"
  }

  it should "correctly extract long object name definitions in nested record classes" in {
    TestTableNames.sampleLongTextColumnDefinition.name shouldEqual "sampleLongTextColumnDefinition"
  }

  it should "correctly name Cassandra Tables" in {
    TestTableNames.tableName shouldEqual "TestTableNames"
  }

  it should "correctly extract the object name " in {
    Test.name shouldEqual "Test"
  }

  it should "correctly extract the table name" in {
    object TestNames extends TestNames
    TestNames.name shouldEqual "TestNames"
  }

  it should "correctly extract the parent name" in {
    object Parent extends Parent
    Parent.name shouldEqual "Parent"
  }

  it should "correctly extract the column names" in {
    object Parent2 extends Parent2
    Parent2.name shouldEqual "Parent2"
  }
}
