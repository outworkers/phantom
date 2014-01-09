package com.newzly.phantom.dsl

import com.newzly.phantom._

import org.scalatest.Matchers

import com.datastax.driver.core.{ Session, Row }
import java.net.InetAddress
import com.twitter.util.{Await, Future}
import java.util.{Date, UUID}
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.{ClassS, Tables}



class IgnoredTests extends BaseTest  with Matchers with Tables {

  implicit val session: Session = cassandraSession

  ignore should "work here but it fails- WE NEED TO FIX IT" in {
    val createTestTable =
      """|CREATE TABLE TestTable2(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        |optionS text
        |mapIntoClass map<text,text>
        );
      """.stripMargin //        #|
    session.execute(createTestTable)

    case class ClassSMap(something: Map[String, Int])
    case class TestRow(key: String, optionA: Option[Int], classS: ClassSMap, optionS: Option[ClassSMap], map: Map[String, ClassSMap])

    class TestTable2 extends CassandraTable[TestTable2, TestRow] {
      def fromRow(r: Row): TestRow = {
        TestRow(key(r), optionA(r), classS(r), optionS(r), mapIntoClass(r))
      }
      object key extends PrimitiveColumn[String]
      object optionA extends OptionalPrimitiveColumn[Int]
      object classS extends JsonTypeColumn[ClassSMap]
      object optionS extends JsonTypeColumn[Option[ClassSMap]]
      object mapIntoClass extends JsonTypeColumn[Map[String, ClassSMap]]
      val _key = key
    }

    val row = TestRow("someKey", Some(2), ClassSMap(Map("k2" -> 5)), Some(ClassSMap(Map("k2" -> 5))), Map("5" -> ClassSMap(Map("p" -> 2))))

    object TestTable2 extends TestTable2 {
      override val tableName = "TestTable2"
    }

    val rcp = TestTable2.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS)
      .value(_.optionS, row.optionS)
      .value(_.mapIntoClass, row.map)

    rcp.qb.enableTracing()
    info(rcp.toString)
    info(rcp.qb.toString)
    rcp.execute().sync()
    val recipeF: Future[Option[TestRow]] = TestTable2.select.one
    assert(recipeF.sync().get === row)
    assert(TestTable2.select.fetch.sync() contains (row))
  }

}
