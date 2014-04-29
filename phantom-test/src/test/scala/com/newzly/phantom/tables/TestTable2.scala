package com.newzly.phantom.tables

import com.newzly.phantom.helper.{ ModelSampler }
import com.newzly.util.testing.Sampler

case class SimpleStringClass(something: String)

object SimpleStringClass extends ModelSampler[SimpleStringClass] {
  def sample: SimpleStringClass = SimpleStringClass(Sampler.getARandomString)
}

case class SimpleMapOfStringsClass(something: Map[String, Int])

object SimpleMapOfStringsClass extends ModelSampler[SimpleMapOfStringsClass] {
  def sample: SimpleMapOfStringsClass = SimpleMapOfStringsClass(Map(
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger(),
    Sampler.getARandomString -> Sampler.getARandomInteger()
  ))
}

case class TestList(key: String, l: List[String])

object TestList extends ModelSampler[TestList] {
  def sample: TestList = TestList(
    Sampler.getARandomString,
    List.range(0, 20).map(x => Sampler.getARandomString)
  )
}

case class SimpleStringModel(something: String) extends ModelSampler[SimpleStringModel] {
  def sample: SimpleStringModel = SimpleStringModel(Sampler.getARandomString)
}

case class TestRow2(
  key: String,
  optionalInt: Option[Int],
  simpleMapOfString: SimpleMapOfStringsClass,
  optionalSimpleMapOfString: Option[SimpleMapOfStringsClass],
  mapOfStringToCaseClass: Map[String, SimpleMapOfStringsClass]
)

object TestRow2 extends ModelSampler[TestRow2] {
  def sample = sample(5)
  def sample(limit: Int = 5): TestRow2 = {
    TestRow2(
      Sampler.getARandomString,
      Some(Sampler.getARandomInteger()),
      SimpleMapOfStringsClass.sample,
      Some(SimpleMapOfStringsClass.sample),
      List.range(0, limit).map(x => { x.toString -> SimpleMapOfStringsClass.sample}).toMap
    )
  }
}
