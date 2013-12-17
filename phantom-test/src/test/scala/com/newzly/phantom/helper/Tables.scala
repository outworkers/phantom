package com.newzly.phantom.helper

import com.newzly.phantom._
import com.datastax.driver.core.{Session, Row}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.{UUID, Date}

case class ClassS(something: String)
case class Author(firstName: String, lastName: String, bio: Option[String])
case class TestList(val key: String, val l: List[String])

trait Tables {
  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10 seconds)
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
  object Primitives extends Primitives {
    override def tableName = "Primitives"
  }


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
  object TestTable extends TestTable {
    override def tableName = "TestTable"
  }



  case class MyTestRow(key: String, optionA: Option[Int], classS: ClassS)

  class MyTest extends CassandraTable[MyTest, MyTestRow] {
    def fromRow(r: Row): MyTestRow = {
      MyTestRow(key(r), optionA(r), classS(r))
    }
    object key extends PrimitiveColumn[String]
    object optionA extends OptionalPrimitiveColumn[Int]
    object classS extends JsonTypeColumn[ClassS]
    val _key = key
  }
  object MyTest extends MyTest {
    override val tableName = "MyTest"
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
  private[helper] def createTables(implicit session: Session): Unit = {
    Primitives.create(_.pkey,
      _.long,
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi).execute().sync()

    val createTestTable =
      """|CREATE TABLE testTable(
        |key text PRIMARY KEY,
        |list list<text>,
        |setText set<text>,
        |mapTextToText map<text,text>,
        |setInt set<int>,
        |mapIntToText map<int,text> );
      """.stripMargin

    session.execute(createTestTable)

    val myTestTable =
      """|CREATE TABLE MyTest(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        );
      """.stripMargin //
    session.execute(myTestTable)

    Recipes.create(_.url,
      _.description,
      _.ingredients,
      _.author,
      _.servings,
      _.last_checked_at,
      _.props,
      _.uid).execute().sync()

    val emptylisttest =
      """|CREATE TABLE emptylisttest(
        |key text PRIMARY KEY,
        |list list<text>
        );
      """.stripMargin //
    session.execute(emptylisttest)

    val listtest =
      """|CREATE TABLE listtest(
        |key text PRIMARY KEY,
        |testlist list<text>
        );
      """.stripMargin //
    session.execute(listtest)

    val articlesTable =
      """|CREATE TABLE articlestest(
        |id uuid PRIMARY KEY,
        |order_id bigint,
        |name text);
      """.stripMargin
    session.execute(articlesTable)

    val indexes = """CREATE INDEX order_id ON articlestest (order_id)""".stripMargin
    session.execute(indexes)
  }


}
