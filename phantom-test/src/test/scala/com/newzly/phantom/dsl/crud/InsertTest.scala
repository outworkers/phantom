package com.newzly.phantom.dsl.crud

import org.scalatest.{Assertions, Matchers}
import com.newzly.phantom.helper._
import com.newzly.phantom.helper.{BaseTest, Tables}
import com.datastax.driver.core.{Row, Session}
import java.net.InetAddress
import com.newzly.phantom._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.SimpleStringClass
import com.newzly.phantom.helper.Author
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import org.scalatest.time.SpanSugar._
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}

class InsertTest  extends BaseTest with Matchers  with Tables with Assertions with AsyncAssertions {
  val keySpace: String = "InsertTestKeySpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  "Insert" should "work fine for primitives columns" in {
    object Primitives extends Primitives {
      override def tableName = "PrimitivesInsertTest"
    }

    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive("myStringInsert", 2.toLong, boolean = true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002
      ))
    val rcp = Primitives.create(_.pkey,
      _.long,
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi)
      .execute() flatMap {
      _ => Primitives.insert
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
        .execute() flatMap {
          _ => {
            for {
              one <- Primitives.select.where(_.pkey eqs "myStringInsert").one
              multi <- Primitives.select.fetch
            } yield (one.get == row, multi.contains(row))
          }
       }
      }

    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    object TestTable extends TestTable {
      override def tableName = "TestTableInsert"
    }

    val row = TestRow("w2", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))

    val createTestTable =
      """|CREATE TABLE TestTableInsert(
        |key text PRIMARY KEY,
        |list list<text>,
        |setText set<text>,
        |mapTextToText map<text,text>,
        |setInt set<int>,
        |mapIntToText map<int,text> );
      """.stripMargin

    session.execute(createTestTable)

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .execute() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs "w2").one
          multi <- TestTable.select.fetch
        }  yield (one.get == row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with custom types" in {
    object MyTest extends MyTest {
      override val tableName = "MyTestInsert"
    }
    val myTestTable =
      """|CREATE TABLE MyTestInsert(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        );
      """.stripMargin //
    session.execute(myTestTable)

    val row = MyTestRow("someKey", Some(2), SimpleStringClass("lol"))

    val rcp = MyTest.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS).execute() flatMap {
      _ =>  {
        for {
          one <- MyTest.select.one
          multi <- MyTest.select.fetch
        }  yield (one.get == row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with Mix" in {
    object Recipes extends Recipes {
      override def tableName = "Recipes"
    }

    val author = Author("Tony", "Clark", Some("great chef..."))
    val r = Recipe("recipe_url", Some("desc"), Seq("ingr1", "ingr2"), Some(author), Some(4), new java.util.Date, Map("a" -> "b", "c" -> "d"))

    val rcp = Recipes.create(_.url,
      _.description,
      _.ingredients,
      _.author,
      _.servings,
      _.last_checked_at,
      _.props,
      _.uid).execute() flatMap { _ => Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.author, r.author)
        .valueOrNull(_.servings, r.servings)
        .value(_.last_checked_at, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased()).execute() flatMap {
        _ => {
         Recipes.select.one
        }
      }
    }
    rcp successful {
      res => {
        assert (res.get == r)
      }
    }
  }

  it should "support serializing/de-serializing empty lists " in {
    class MyTest extends CassandraTable[MyTest, TestList] {
      def fromRow(r: Row): TestList = TestList(key(r), list(r))
      object key extends PrimitiveColumn[String]
      object list extends ListColumn[String]
      val _key = key
    }

    val emptylisttest =
      """|CREATE TABLE emptylisttest(
        |key text PRIMARY KEY,
        |list list<text>
        );
      """.stripMargin //
    session.execute(emptylisttest)

    val row = TestList("someKey", Nil)

    object MyTest extends MyTest {
      override val tableName = "emptylisttest"
    }

    val f = MyTest.insert.value(_.key, row.key).value(_.list, row.l).execute() flatMap {
      _ => MyTest.select.one
    }

    f successful  {
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
    val listtest =
      """|CREATE TABLE listtest(
        |key text PRIMARY KEY,
        |testlist list<text>
        );
      """.stripMargin //
    session.execute(listtest)

    val row = TestList("someKey", List("test", "test2"))

    object MyTest extends MyTest {
      override val tableName = "listtest"
    }

    val recipeF = MyTest.insert.value(_.key,row.key).value(_.testlist,row.l).execute() flatMap {
      _ => MyTest.select.one
    }

    recipeF successful  {
      case res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }
  }

}
