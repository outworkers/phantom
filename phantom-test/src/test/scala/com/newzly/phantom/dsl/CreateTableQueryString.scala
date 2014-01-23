package com.newzly.phantom.dsl

import org.scalatest.FlatSpec
import com.newzly.phantom._
import com.datastax.driver.core.Row
import com.twitter.util.{Await, Future}
import com.twitter.conversions.time._
import java.util.{UUID, Date}

class CreateTableQueryString extends FlatSpec {

  it should "get the right query in primitives table" in {
    case class Primitive(
      key: Int,
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
        Primitive(keyName(r), longName(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
          int(r), date(r), uuid(r), bi(r))
      }
      object keyName extends PrimitiveColumn[Int]
      object longName extends PrimitiveColumn[Long]
      object boolean extends PrimitiveColumn[Boolean]
      object bDecimal extends PrimitiveColumn[BigDecimal]
      object double extends PrimitiveColumn[Double]
      object float extends PrimitiveColumn[Float]
      object inet extends PrimitiveColumn[java.net.InetAddress]
      object int extends PrimitiveColumn[Int]
      object date extends PrimitiveColumn[java.util.Date]
      object uuid extends PrimitiveColumn[java.util.UUID]
      object bi extends PrimitiveColumn[BigInt]
      val _key = keyName
    }
    object Primitives extends Primitives {
      override def tableName = "Primitives"
    }
    assert(Primitives.tableName === "Primitives")
    val q = Primitives.create(_.keyName,
      _.longName,
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi).queryString

    assert(q.stripMargin === "CREATE TABLE Primitives " +
        "( keyName int, " +
        "longName bigint, " +
        "boolean boolean, " +
        "bDecimal decimal, " +
        "double double, " +
        "float float, " +
        "inet inet, " +
        "int int, " +
        "date timestamp, " +
        "uuid uuid, " +
        "bi varint, " +
        "PRIMARY KEY (keyName));")
  }

  it should "work fine with List, Set, Map" in {
    case class TestRow(key: String,
                       list: Seq[String],
                       setText: Set[String],
                       mapTextToText: Map[String, String],
                       setInt: Set[Int],
                       mapIntToText: Map[Int, String])

    class TestTable extends CassandraTable[TestTable, TestRow]{
      object key extends PrimitiveColumn[String]
      object list extends SeqColumn[String]
      object setText extends SetColumn[String]
      object mapTextToText extends MapColumn[String, String]
      object setInt extends  SetColumn[Int]
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
    val row = TestRow("w", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))
    object TestTable extends TestTable {
      override def tableName = "TestTable"
    }
    val q = TestTable.create(_.key,_.list,_.setText,_.mapTextToText,_.setInt, _.mapIntToText).queryString
    assert( q==="CREATE TABLE TestTable " +
      "( key text, " +
      "list list<text>, " +
      "setText set<text>, " +
      "mapTextToText map<text, text>, " +
      "setInt set<int>, " +
      "mapIntToText map<int, text>, " +
      "PRIMARY KEY (key));")
  }

  it should "get the right query in mix table" in {
    case class Recipe(
                       url: String,
                       description: Option[String],
                       ingredients: Seq[String],
                       author: Option[Author],
                       servings: Option[Int],
                       lastCheckedAt: java.util.Date,
                       props: Map[String, String])
    case class Author(firstName: String, lastName: String, bio: Option[String])

    class Recipes extends CassandraTable[Recipes, Recipe] {

      override def fromRow(r: Row): Recipe = {
        Recipe(url(r), description(r), ingredients(r), author.optional(r), servings(r), last_checked_at(r), props(r))
      }

      object url extends PrimitiveColumn[String]
      object description extends OptionalPrimitiveColumn[String]
      object ingredients extends SeqColumn[String]
      object author extends JsonTypeColumn[Author]
      object servings extends OptionalPrimitiveColumn[Int]
      object last_checked_at extends PrimitiveColumn[Date]
      object props extends MapColumn[String, String]
      object uid extends PrimitiveColumn[UUID]
      val _key = url
    }
    object Recipes extends Recipes {
      override def tableName = "Recipes"
    }
    val q = Recipes.create(_.url,
      _.description,
      _.ingredients,
      _.author,
      _.servings,
      _.last_checked_at,
      _.props,
      _.uid).queryString

    assert(q.stripMargin === "CREATE TABLE Recipes ( "+
      "url text, " +
      "description text, " +
      "ingredients list<text>, " +
      "author text, " +
      "servings int, " +
      "last_checked_at timestamp, " +
      "props map<text, text>, " +
      "uid uuid, " +
      "PRIMARY KEY (url));")
  }
}

