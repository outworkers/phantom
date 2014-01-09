package com.newzly.phantom.dsl.crud

import com.newzly.phantom.dsl.BaseTest
import com.twitter.util.Future
import org.scalatest.Matchers
import com.newzly.phantom.helper._
import com.datastax.driver.core.{Row, Session}
import java.net.InetAddress
import com.newzly.phantom._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.ClassS
import com.newzly.phantom.helper.Author
import scala.Some

class InsertTest  extends BaseTest with Matchers with Tables{

  implicit val session: Session = cassandraSession
  "Insert" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java

    val row = Primitive("myStringInsert", 2.toLong, boolean = true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002
      ))
    val rcp = Primitives.insert
      .value(_.pkey, row.pkey)
      .value(_.long, row.long)
      .value(_.boolean, row.boolean)
      .value(_.bDecimal, row.bDecimal)
      .value(_.double, row.double)
      .value(_.float, row.float)
      .value(_.inet, row.inet)
      .value(_.int, row.int)
      .value(_.date, row.date)
      .value(_.uuid, row.uuid)
      .value(_.bi, row.bi)
    rcp.execute().sync()
    val recipeF: Future[Option[Primitive]] = Primitives.select.where(_.pkey eqs "myStringInsert").one
    assert(recipeF.sync().get === row)

    assert(Primitives.select.fetch.sync() contains row)
  }

  it should "work fine with List, Set, Map" in {

    val row = TestRow("w2", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)

    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable.select.where(_.key eqs "w2").one
    assert(recipeF.sync().get === row)

    assert(TestTable.select.fetch.sync() contains row)
  }


  it should "work fine with custom types" in {
    val row = MyTestRow("someKey", Some(2), ClassS("lol"))

    val rcp = MyTest.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS)
    rcp.execute().sync()
    val recipeF: Future[Option[MyTestRow]] = MyTest.select.one
    assert(recipeF.sync().get === row)

    assert(MyTest.select.fetch.sync() contains row)
  }

  it should "work fine with Mix" in {
    implicit val formats = net.liftweb.json.DefaultFormats
    val author = Author("Tony", "Clark", Some("great chef..."))
    val r = Recipe("recipe_url", Some("desc"), Seq("ingr1", "ingr2"), Some(author), Some(4), new java.util.Date, Map("a" -> "b", "c" -> "d"))

    val rcp = Recipes.insert
      .value(_.url, r.url)
      .valueOrNull(_.description, r.description)
      .value(_.ingredients, r.ingredients)
      .valueOrNull(_.author, r.author)
      .valueOrNull(_.servings, r.servings)
      .value(_.last_checked_at, r.lastCheckedAt)
      .value(_.props, r.props)
      .value(_.uid, UUIDs.timeBased())

    rcp.execute().sync()

    val recipeF: Future[Option[Recipe]] = Recipes.select.one
    recipeF.sync()

  }
  it should "support serializing/de-serializing empty lists " in {
    class MyTest extends CassandraTable[MyTest, TestList] {
      def fromRow(r: Row): TestList = TestList(key(r), list(r))
      object key extends PrimitiveColumn[String]
      object list extends ListColumn[String]
      val _key = key
    }

    val row = TestList("someKey", Nil)

    object MyTest extends MyTest {
      override val tableName = "emptylisttest"
    }

    MyTest.insert.value(_.key, row.key).value(_.list, row.l).execute().sync()

    val future = MyTest.select.one
    future.onSuccess {
      res => res.isEmpty shouldEqual false
    }
  }
  it should "support serializing/de-serializing to List " in {
    case class TestList(key: String, l: List[String])

    class MyTest extends CassandraTable[MyTest, TestList] {
      def fromRow(r: Row): TestList = {
        TestList(key(r), testlist(r))
      }
      object key extends PrimitiveColumn[String]
      object testlist extends ListColumn[String]
      val _key = key
    }

    val row = TestList("someKey", List("test", "test2"))

    object MyTest extends MyTest {
      override val tableName = "listtest"
    }
    MyTest.insert.value(_.key,row.key).value(_.testlist,row.l).execute().sync()
    val recipeF: Future[Option[TestList]] = MyTest.select.one
    recipeF.onSuccess {
      res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }

  }
}
