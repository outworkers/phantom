package com.newzly.phantom.helper

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.field.{LongOrderKey, UUIDPk}

case class SimpleStringClass(something: String)

case class SimpleMapOfStringsClass(something: Map[String, Int])

case class TestList(key: String, l: List[String])


case class TestRow(
  key: String,
  list: Seq[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String]
)

case class TestRow2(
  key: String,
  optionalInt: Option[Int],
  simpleMapOfString: SimpleMapOfStringsClass,
  optionalSimpleMapOfString: Option[SimpleMapOfStringsClass],
  mapOfStringToCaseClass: Map[String, SimpleMapOfStringsClass]
)

case class T(something: String)

case class JsonSeqColumnRow(pkey: String, jtsc: Seq[Recipe])







class TestTable extends CassandraTable[TestTable, TestRow] {

  object key extends PrimitiveColumn[String]

  object list extends SeqColumn[String]

  object setText extends SetColumn[String]

  object mapTextToText extends MapColumn[String, String]

  object setInt extends SetColumn[Int]

  object mapIntToText extends MapColumn[Int, String]

  def fromRow(r: Row): TestRow = {
    TestRow(
      key(r),
      list(r),
      setText(r),
      mapTextToText(r),
      setInt(r).toSet,
      mapIntToText(r)
    )
  }

  val _key = key
}

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


case class MyTestRow(key: String, optionA: Option[Int], classS: SimpleStringClass)

class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), classS(r))
  }

  object key extends PrimitiveColumn[String]

  object optionA extends OptionalPrimitiveColumn[Int]

  object classS extends JsonColumn[SimpleStringClass]

  val _key = key
}



