package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._

trait Test {
  private[this] lazy val _name: String = {
    val fullName = getClass.getName.split("\\.").toList.last
    val index = fullName.indexOf("$$anonfun")

    if (index != -1) {
      val str = fullName.substring(index + 9, fullName.length)
      str.replaceAll("[(\\$\\d+\\$)]", "")
    } else {
      fullName.replaceAll("[(\\$\\d+\\$)]", "")
    }
  }
  val name: String = _name
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
    val fullName = getClass.getName.split("\\.").toList.last
    val index = fullName.indexOf("$$anonfun")
    val str = fullName.substring(index + 9, fullName.length)
    str.replaceAll("(\\$\\d+\\$)", "")
  }
  def name: String = _name
}

class Parent extends TestNames
class Parent2 extends Parent

class ClassNameExtraction extends FlatSpec {

  it should "get rid of extra naming inside the object" in {
    val test = "$$anonfun23primitives3key$"
    val res = test.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)", "")
    assert(res === "key")
  }

  it should "correctly name objects inside record classes " in {
    assert(TestTableNames.record.name === "record")
  }

  it should "correctly extract long object name definitions in nested record classes" in {
    assert(TestTableNames.sampleLongTextColumnDefinition.name === "sampleLongTextColumnDefinition")
  }

  it should "correctly name Cassandra Tables" in {
    assert(TestTableNames.tableName === "TestTableNames")
  }

  it should "correctly extract the object name " in {
    assert(Test.name === "Test")
  }

  it should "correctly extract the table name" in {
    object TestNames extends TestNames

    assert(TestNames.name === "TestNames")
  }

  it should "correctly extract the parent name" in {
    object Parent extends Parent

    assert(Parent.name === "Parent")
  }

  it should "correctly extract the column names" in {
    object Parent2 extends Parent2
    assert(Parent2.name === "Parent2")
  }

}
