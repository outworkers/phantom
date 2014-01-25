package com.newzly.phantom.helper

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.field.{LongOrderKey, UUIDPk}

case class SimpleStringClass(something: String)

case class Author(
  firstName: String,
  lastName: String,
  bio: Option[String]
)

case class SimpleMapOfStringsClass(something: Map[String, Int])

case class TestList(key: String, l: List[String])

case class Primitive(
  pkey: String,
  long: Long,
  boolean: Boolean,
  bDecimal: BigDecimal,
  double: Double,
  float: Float,
  inet: java.net.InetAddress,
  int: Int,
  date: java.util.Date,
  uuid: java.util.UUID,
  bi: BigInt
)

case class TestRow(
  key: String,
  list: Seq[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String],
  mapIntToString: Map[Int, String]
)

case class TestRow2(
  key: String,
  list: Seq[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String]
)

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: Seq[String],
  author: Option[Author],
  servings: Option[Int],
  lastCheckedAt: java.util.Date,
  props: Map[String, String]
)

case class T(something: String)

case class JsonSeqColumnRow(pkey: String, jtsc: Seq[Recipe])

case class Article(name: String, id: UUID, order_id: Long)

class Articles extends CassandraTable[Articles, Article] with UUIDPk[Articles] with LongOrderKey[Articles] {
  object name extends PrimitiveColumn[String]
  override def fromRow(row: Row): Article = {
    Article(name(row), id(row), order_id(row))
  }
}

class Primitives extends CassandraTable[Primitives, Primitive] {
  override def fromRow(r: Row): Primitive = {
    Primitive(pkey(r), long(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
      int(r), date(r), uuid(r), bi(r))
  }

  object pkey extends PrimitiveColumn[String]

  object long extends PrimitiveColumn[Long]

  object boolean extends PrimitiveColumn[Boolean]

  object bDecimal extends PrimitiveColumn[BigDecimal]

  object double extends PrimitiveColumn[Double]

  object float extends PrimitiveColumn[Float]

  object inet extends PrimitiveColumn[java.net.InetAddress]

  object int extends PrimitiveColumn[Int]

  object date extends PrimitiveColumn[java.util.Date]

  object uuid extends PrimitiveColumn[java.util.UUID]

  object bi extends PrimitiveColumn[BigInt]

  val _key = pkey
}

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
    TestRow2(key(r), optionA(r), classS(r), optionS(r), mapIntoClass(r))
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


class Recipes extends CassandraTable[Recipes, Recipe] {
  override def fromRow(r: Row): Recipe = {
    Recipe(url(r), description(r), ingredients(r), author.optional(r), servings(r), last_checked_at(r), props(r))
  }
  object url extends PrimitiveColumn[String]

  object description extends OptionalPrimitiveColumn[String]

  object ingredients extends SeqColumn[String]

  object author extends JsonColumn[Author]

  object servings extends OptionalPrimitiveColumn[Int]

  object last_checked_at extends PrimitiveColumn[Date]

  object props extends MapColumn[String, String]

  object uid extends PrimitiveColumn[UUID]

  val _key = url
}
