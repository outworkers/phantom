package com.newzly.phantom.tables

import com.newzly.phantom.{JsonColumn, OptionalPrimitiveColumn, PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.Row

case class SimpleStringClass(something: String)

case class SimpleMapOfStringsClass(something: Map[String, Int])

case class TestList(key: String, l: List[String])

case class T(something: String)

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
