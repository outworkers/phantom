package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.{
  CassandraTable,
  JsonColumn,
  OptionalPrimitiveColumn,
  PrimitiveColumn
}
import com.newzly.phantom.helper.{ ModelSampler, Sampler }

case class SimpleStringClass(something: String) extends ModelSampler {
  def sample: SimpleStringClass = SimpleStringClass(Sampler.getAUniqueString)
}

case class SimpleMapOfStringsClass(something: Map[String, Int]) extends ModelSampler {
  def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(Map(
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger(),
    Sampler.getAUniqueString -> Sampler.getARandomInteger()
  ))
}

case class TestList(key: String, l: List[String]) extends ModelSampler {
   def sample: TestList = TestList(
    Sampler.getAUniqueString,
    List.range(0, 20).map(x => Sampler.getAUniqueString)
   )
}

case class T(something: String) extends ModelSampler {
  def sample: T = T(Sampler.getAUniqueString)
}

case class TestRow2(
  key: String,
  optionalInt: Option[Int],
  simpleMapOfString: SimpleMapOfStringsClass,
  optionalSimpleMapOfString: Option[SimpleMapOfStringsClass],
  mapOfStringToCaseClass: Map[String, SimpleMapOfStringsClass]
)

class TestTable2 extends CassandraTable[TestTable2, TestRow2] {
  def fromRow(r: Row): TestRow2 = {
    TestRow2(
      key(r),
      optionA(r),
      classS(r),
      optionS(r),
      mapIntoClass(r)
    )
  }
  object key extends PrimitiveColumn[String]
  object optionA extends OptionalPrimitiveColumn[Int]
  object classS extends JsonColumn[SimpleMapOfStringsClass]
  object optionS extends JsonColumn[Option[SimpleMapOfStringsClass]]
  object mapIntoClass extends JsonColumn[Map[String, SimpleMapOfStringsClass]]
  val _key = key
}
