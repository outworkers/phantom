package com.newzly.cassandra.phantom.dsl

import com.newzly.cassandra.phantom.{AbstractColumn, CassandraTable}
import scala.concurrent.ExecutionContext.Implicits.global
import com.datastax.driver.core.{ Session, Row }
import scala.concurrent.{ Await, Future }
import java.net.InetAddress
import scala.concurrent.duration.Duration
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core
import com.datastax.driver.core.querybuilder.{QueryBuilder, Assignment}
import com.newzly.cassandra.phantom.query.Operators

class CRUDTests extends BaseTest {

  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, Duration(10, "seconds"))
    }
  }

  implicit val session: Session = cassandraSession


  "Delete" should "work fine, when deleting the whole row" in {
    val primitivesTable =
      """|CREATE TABLE primitives(
        |str text PRIMARY KEY,
        |long bigint,
        |boolean boolean,
        |bDecimal decimal,
        |double double,
        |float float,
        |inet inet,
        |int int,
        |date timestamp,
        |uuid uuid,
        |bi varint);
      """.stripMargin
    cassandraSession.execute(primitivesTable)

    case class Primitives(
                           str: String,
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

    class PrimitivesTable extends CassandraTable[PrimitivesTable, Primitives]("primitives") {
      override def fromRow(r: Row): Primitives = {
        Primitives(str(r), long(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
          int(r), date(r), uuid(r), bi(r))
      }

      val str = column[String]("str")
      val long = column[Long]("long")
      val boolean = column[Boolean]("boolean")
      val bDecimal = column[BigDecimal]("bDecimal")
      val double = column[Double]("double")
      val float = column[Float]("float")
      val inet = column[java.net.InetAddress]("inet")
      val int = column[Int]("int")
      val date = column[java.util.Date]("date")
      val uuid = column[java.util.UUID]("uuid")
      val bi = column[BigInt]("bi")

    }
    object PrimitivesTable extends PrimitivesTable

    val row = Primitives("myString", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002))
    val rcp = PrimitivesTable.insert
      .value(_.str, row.str)
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
    val recipeF: Future[Option[Primitives]] = PrimitivesTable.select.one
    assert(recipeF.sync().get === row)
    assert(PrimitivesTable.select.fetch.sync() contains (row))

    val del = PrimitivesTable.delete where(_.str,"myString",Operators.EQ[PrimitivesTable,String])
    del.execute().sync()

    val recipeF2: Future[Option[Primitives]] = PrimitivesTable.select.one
    val rowFromDb = recipeF2.sync()
    assert(rowFromDb.isEmpty)
  }

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val primitivesTable =
      """|CREATE TABLE primitives(
        |str text PRIMARY KEY,
        |long bigint,
        |boolean boolean,
        |bDecimal decimal,
        |double double,
        |float float,
        |inet inet,
        |int int,
        |date timestamp,
        |uuid uuid,
        |bi varint);
      """.stripMargin
    cassandraSession.execute(primitivesTable)

    case class Primitives(
                           str: String,
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

    class PrimitivesTable extends CassandraTable[PrimitivesTable, Primitives]("primitives") {
      override def fromRow(r: Row): Primitives = {
        Primitives(str(r), long(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
          int(r), date(r), uuid(r), bi(r))
      }

      val str = column[String]("str")
      val long = column[Long]("long")
      val boolean = column[Boolean]("boolean")
      val bDecimal = column[BigDecimal]("bDecimal")
      val double = column[Double]("double")
      val float = column[Float]("float")
      val inet = column[java.net.InetAddress]("inet")
      val int = column[Int]("int")
      val date = column[java.util.Date]("date")
      val uuid = column[java.util.UUID]("uuid")
      val bi = column[BigInt]("bi")

    }
    object PrimitivesTable extends PrimitivesTable

    val row = Primitives("myString", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002))
    val rcp = PrimitivesTable.insert
      .value(_.str, row.str)
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
    val recipeF: Future[Option[Primitives]] = PrimitivesTable.select.one
    assert(recipeF.sync().get === row)
    assert(PrimitivesTable.select.fetch.sync() contains (row))

    val updatedRow = Primitives("myString", 21.toLong, true, BigDecimal("11.11"), 31.toDouble, 41.toFloat,
      InetAddress.getByName("127.1.1.1"), 911, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1012))

    PrimitivesTable.update.
      //where(PrimitivesTable => QueryBuilder.eq("str", "myString"))
      where(_.str,"myString",Operators.EQ[PrimitivesTable,String])
      .modify(_.long, updatedRow.long)
      .modify(_.boolean, updatedRow.boolean)
      .modify(_.bDecimal, updatedRow.bDecimal)
      .modify(_.double, updatedRow.double)
      .modify(_.float, updatedRow.float)
      .modify(_.inet, updatedRow.inet)
      .modify(_.int, updatedRow.int)
      .modify(_.date, updatedRow.date)
      .modify(_.uuid, updatedRow.uuid)
      .modify(_.bi, updatedRow.bi).execute().sync()

    val recipeF2: Future[Option[Primitives]] = PrimitivesTable.select.one
    val rowFromDb = recipeF2.sync().get
    assert( rowFromDb === updatedRow)
    assert(PrimitivesTable.select.fetch.sync() contains (updatedRow))
  }

  it should "work fine with List, Set, Map" in {
    val createTestTable =
      """|CREATE TABLE testTable(
        |key text PRIMARY KEY,
        |list list<text>,
        |setText set<text>,
        |mapTextToText map<text,text>,
        |setInt set<int>,
        |mapIntToText map<int,text> );
      """.stripMargin

    cassandraSession.execute(createTestTable)

    case class TestRow(key: String,
                       list: Seq[String],
                       setText: Set[String],
                       mapTextToText: Map[String, String],
                       setInt: Set[Int],
                       mapIntToText: Map[Int, String])

    class TestTable extends CassandraTable[TestTable, TestRow]("testTable") {
      val key = column[String]("key")
      val list = seqColumn[String]("list")
      val setText = setColumn[String]("setText")
      val mapTextToText = mapColumn[String, String]("mapTextToText")
      val setInt = setColumn[Int]("setInt")
      val mapIntToText = mapColumn[Int, String]("mapIntToText")

      def fromRow(r: Row): TestRow = {
        TestRow(key(r), list(r),
          setText(r),
          mapTextToText(r),
          setInt(r).toSet,
          mapIntToText(r))
      }
    }
    val row = TestRow("w", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))
    object TestTable extends TestTable
    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)

    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable.select.one
    assert(recipeF.sync().get === row)
    assert(TestTable.select.fetch.sync() contains (row))
    val updatedRow = row.copy(
      list = Seq ("new"),
      setText = Set("newSet"),
      mapTextToText =  Map("n" -> "newVal"),
      setInt = Set(3,4,7),
      mapIntToText = Map (-1 -> "&&&")
    )

    TestTable.update
      .where(_.key,"w",Operators.EQ[TestTable,String])
      .modify(_.list,updatedRow.list)
      .modify(_.setText,updatedRow.setText)
      .modify(_.mapTextToText,updatedRow.mapTextToText)
      .modify(_.setInt,updatedRow.setInt)
      .modify(_.mapIntToText,updatedRow.mapIntToText).execute().sync()

    val recipeF2: Future[Option[TestRow]] = TestTable.select.one
    val rowFromDb = recipeF2.sync().get
    assert( rowFromDb === updatedRow)
    assert(TestTable.select.fetch.sync() contains (updatedRow))

  }

  "Insert" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val primitivesTable =
      """|CREATE TABLE primitives(
        |str text PRIMARY KEY,
        |long bigint,
        |boolean boolean,
        |bDecimal decimal,
        |double double,
        |float float,
        |inet inet,
        |int int,
        |date timestamp,
        |uuid uuid,
        |bi varint);
      """.stripMargin
    cassandraSession.execute(primitivesTable)

    case class Primitives(
      str: String,
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

    class PrimitivesTable extends CassandraTable[PrimitivesTable, Primitives]("primitives") {
      override def fromRow(r: Row): Primitives = {
        Primitives(str(r), long(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
          int(r), date(r), uuid(r), bi(r))
      }

      val str = column[String]("str")
      val long = column[Long]("long")
      val boolean = column[Boolean]("boolean")
      val bDecimal = column[BigDecimal]("bDecimal")
      val double = column[Double]("double")
      val float = column[Float]("float")
      val inet = column[java.net.InetAddress]("inet")
      val int = column[Int]("int")
      val date = column[java.util.Date]("date")
      val uuid = column[java.util.UUID]("uuid")
      val bi = column[BigInt]("bi")

    }
    object PrimitivesTable extends PrimitivesTable

    val row = Primitives("myString", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002
      ))
    val rcp = PrimitivesTable.insert
      .value(_.str, row.str)
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
    val recipeF: Future[Option[Primitives]] = PrimitivesTable.select.one
    assert(recipeF.sync().get === row)

    assert(PrimitivesTable.select.fetch.sync() contains (row))
  }

  it should "work fine with List, Set, Map" in {
    val createTestTable =
      """|CREATE TABLE testTable(
        |key text PRIMARY KEY,
        |list list<text>,
        |setText set<text>,
        |mapTextToText map<text,text>,
        |setInt set<int>,
        |mapIntToText map<int,text> );
      """.stripMargin

    cassandraSession.execute(createTestTable)

    case class TestRow(key: String,
      list: Seq[String],
      setText: Set[String],
      mapTextToText: Map[String, String],
      setInt: Set[Int],
      mapIntToText: Map[Int, String])

    class TestTable extends CassandraTable[TestTable, TestRow]("testTable") {
      val key = column[String]("key")
      val list = seqColumn[String]("list")
      val setText = setColumn[String]("setText")
      val mapTextToText = mapColumn[String, String]("mapTextToText")
      val setInt = setColumn[Int]("setInt")
      val mapIntToText = mapColumn[Int, String]("mapIntToText")

      def fromRow(r: Row): TestRow = {
        TestRow(key(r), list(r),
          setText(r),
          mapTextToText(r),
          setInt(r).toSet,
          mapIntToText(r))
      }
    }
    val row = TestRow("w", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))
    object TestTable extends TestTable
    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)

    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable.select.one
    assert(recipeF.sync().get === row)

    assert(TestTable.select.fetch.sync() contains (row))
  }

  it should "work fine with custom types" in {
    val createTestTable =
      """|CREATE TABLE myTest(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        );
      """.stripMargin //
    session.execute(createTestTable)

    case class ClassS(something: String)
    //case class ClassS(something:Map[String,Int])
    case class TestRow(key: String, optionA: Option[Int], classS: ClassS)

    class TestTable extends CassandraTable[TestTable, TestRow]("myTest") {
      def fromRow(r: Row): TestRow = {
        TestRow(key(r), optionA(r), classS(r))
      }
      val key = column[String]("key")
      val optionA = optColumn[Int]("optionA")
      val classS = jsonColumn[ClassS]("classS")
    }

    val row = TestRow("someKey", Some(2), ClassS("lol"))
    object TestTable extends TestTable
    val rcp = TestTable.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS)
    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable.select.one
    assert(recipeF.sync().get === row)

    assert(TestTable.select.fetch.sync() contains (row))
  }

  it should "work fine with Mix" in {
    val recipesTable =
      """|CREATE TABLE recipes (
        |url text PRIMARY KEY,
        |description text,
        |ingredients list<text>,
        |author text,
        |servings int,
        |tags set<text>,
        |last_checked_at timestamp,
        |props map<text, text>,
        |uid timeuuid);
      """.stripMargin
    session.execute(recipesTable)

    case class Author(firstName: String, lastName: String, bio: Option[String])

    case class Recipe(
      url: String,
      description: Option[String],
      ingredients: Seq[String],
      author: Option[Author],
      servings: Option[Int],
      lastCheckedAt: java.util.Date,
      props: Map[String, String])

    class Recipes extends CassandraTable[Recipes, Recipe]("recipes") {

      override def fromRow(r: Row): Recipe = {
        Recipe(url(r), description(r), ingredients(r), author.optional(r), servings(r), lastCheckedAt(r), props(r))
      }

      val url = column[String]("url")
      val description = optColumn[String]("description")
      val ingredients = seqColumn[String]("ingredients")
      val author = jsonColumn[Author]("author")
      val servings = optColumn[Int]("servings")
      val lastCheckedAt = column[java.util.Date]("last_checked_at")
      val props = mapColumn[String, String]("props")
      val uid = column[UUID]("uid")
    }
    implicit val formats = net.liftweb.json.DefaultFormats
    val author = Author("Tony", "Clark", Some("great chef..."))
    val r = Recipe("recipe_url", Some("desc"), Seq("ingr1", "ingr2"), Some(author), Some(4), new java.util.Date, Map("a" -> "b", "c" -> "d"))

    object Recipes extends Recipes
    val rcp = Recipes.insert
      .value(_.url, r.url)
      .valueOrNull(_.description, r.description)
      .value(_.ingredients, r.ingredients)
      .valueOrNull(_.author, r.author)
      .valueOrNull(_.servings, r.servings)
      .value(_.lastCheckedAt, r.lastCheckedAt)
      .value(_.props, r.props)
      .value(_.uid, UUIDs.timeBased())

    rcp.execute().sync()

    val recipeF: Future[Option[Recipe]] = Recipes.select.one
    recipeF.sync()

  }

  ignore should "work here but it fails- WE NEED TO FIX IT" in {
    val createTestTable =
      """|CREATE TABLE myTest(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        |optionS text
        |mapIntoClass map<text,text>
        );
      """.stripMargin //        #|
    session.execute(createTestTable)

    case class ClassS(something: Map[String, Int])
    case class TestRow(key: String, optionA: Option[Int], classS: ClassS, optionS: Option[ClassS], map: Map[String, ClassS])

    class TestTable extends CassandraTable[TestTable, TestRow]("myTest") {
      def fromRow(r: Row): TestRow = {
        TestRow(key(r), optionA(r), classS(r), optionS(r), mapIntoClass(r))
      }
      val key = column[String]("key")
      val optionA = optColumn[Int]("optionA")
      val classS = jsonColumn[ClassS]("classS")
      val optionS = jsonColumn[Option[ClassS]]("optionS")
      val mapIntoClass = jsonColumn[Map[String, ClassS]]("mapIntoClass")
    }

    val row = TestRow("someKey", Some(2), ClassS(Map("k2" -> 5)), Some(ClassS(Map("k2" -> 5))), Map("5" -> ClassS(Map("p" -> 2))))
    object TestTable extends TestTable
    val rcp = TestTable.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS)
      .value(_.optionS, row.optionS)
      .value(_.mapIntoClass, row.map)
    rcp.qb.enableTracing()
    info(rcp.toString)
    info(rcp.qb.toString)
    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable.select.one
    assert(recipeF.sync().get === row)

    assert(TestTable.select.fetch.sync() contains (row))
  }

}
