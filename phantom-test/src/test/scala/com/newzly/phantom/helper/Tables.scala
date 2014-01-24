package com.newzly.phantom.helper

import com.newzly.phantom._
import com.datastax.driver.core.{Session, Row}
import java.util.{UUID, Date}
import com.twitter.util.{Await, Future}
import com.twitter.conversions.time._

case class ClassS(something: String)

case class Author(firstName: String, lastName: String, bio: Option[String])

case class TestList(key: String, l: List[String])

case class TestRow(key: String,
                   list: Seq[String],
                   setText: Set[String],
                   mapTextToText: Map[String, String],
                   setInt: Set[Int],
                   mapIntToText: Map[Int, String]
                    )


trait Tables {

  private[this] implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10.seconds)
    }
  }

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
                        bi: BigInt)

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
      TestRow(key(r), list(r),
        setText(r),
        mapTextToText(r),
        setInt(r).toSet,
        mapIntToText(r))
    }

    val _key = key
  }

  case class MyTestRow(key: String, optionA: Option[Int], classS: ClassS)

  class MyTest extends CassandraTable[MyTest, MyTestRow] {
    def fromRow(r: Row): MyTestRow = {
      MyTestRow(key(r), optionA(r), classS(r))
    }

    object key extends PrimitiveColumn[String]

    object optionA extends OptionalPrimitiveColumn[Int]

    object classS extends JsonColumn[ClassS]

    val _key = key
  }

  case class Recipe(
                     url: String,
                     description: Option[String],
                     ingredients: Seq[String],
                     author: Option[Author],
                     servings: Option[Int],
                     lastCheckedAt: java.util.Date,
                     props: Map[String, String])

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
}
