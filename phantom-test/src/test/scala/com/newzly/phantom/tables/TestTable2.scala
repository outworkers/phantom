package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.column.JsonColumn
import com.newzly.phantom.keys.PrimaryKey

case class SimpleStringClass(something: String)

object SimpleStringClass extends ModelSampler[SimpleStringClass] {
  def sample: SimpleStringClass = SimpleStringClass(Sampler.getAUniqueString)
}

case class SimpleMapOfStringsClass(something: Map[String, Int])

object SimpleMapOfStringsClass extends ModelSampler[SimpleMapOfStringsClass] {
  def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(Map(
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger()
  ))
}

case class TestList(key: String, l: List[String])

object TestList extends ModelSampler[TestList] {
  def sample: TestList = TestList(
    Sampler.getAUniqueString,
    List.range(0, 20).map(x => Sampler.getAUniqueString)
  )
}

case class SimpleStringModel(something: String) extends ModelSampler[SimpleStringModel] {
  def sample: SimpleStringModel = SimpleStringModel(Sampler.getAUniqueString)
}

case class TestRow2(
  key: String,
  optionalInt: Option[Int],
  simpleMapOfString: SimpleMapOfStringsClass,
  optionalSimpleMapOfString: Option[SimpleMapOfStringsClass],
  mapOfStringToCaseClass: Map[String, SimpleMapOfStringsClass]
)

object TestRow2 extends ModelSampler[TestRow2] {
  def sample: TestRow2 = {
    TestRow2(
      Sampler.getAUniqueString,
      Some(Sampler.getARandomInteger()),
      SimpleMapOfStringsClass.sample,
      Some(SimpleMapOfStringsClass.sample),
      List.range(0, 20).map(x => { x.toString -> SimpleMapOfStringsClass.sample}).toMap
    )
  }
}

sealed class TestTable2 extends CassandraTable[TestTable2, TestRow2] {
  def fromRow(r: Row): TestRow2 = {
    TestRow2(
      key(r),
      optionA(r),
      classS(r),
      optionS(r),
      mapIntoClass(r)
    )
  }

  def meta = TestTable2

  object key extends StringColumn(this) with PrimaryKey[TestTable2, TestRow2]
  object optionA extends OptionalIntColumn(this)
  object classS extends JsonColumn[TestTable2, TestRow2, SimpleMapOfStringsClass](this)
  object optionS extends JsonColumn[TestTable2, TestRow2, Option[SimpleMapOfStringsClass]](this)
  object mapIntoClass extends JsonColumn[TestTable2, TestRow2, Map[String, SimpleMapOfStringsClass]](this)
  def createSchema: String = super.create().queryString
}

object TestTable2 extends TestTable2 with TestSampler[TestTable2, TestRow2] {
  override val tableName = "TestTable2"
}