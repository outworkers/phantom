package com.newzly.phantom.dsl

import org.apache.log4j.Logger
import org.scalatest.Matchers
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}

import com.datastax.driver.core.{ Session, Row }
import com.datastax.driver.core.exceptions.SyntaxError
import com.newzly.phantom._
import com.newzly.phantom.helper.{BaseTest => MyBaseTest, Tables}
import com.twitter.util.{Await, Duration, Future, NonFatal}

case class ClassSMap(something: Map[String, Int])
case class TestRow(key: String, optionA: Option[Int], classS: ClassSMap, optionS: Option[ClassSMap], map: Map[String, ClassSMap])

class JsonTypeSeqTest extends MyBaseTest  with Matchers with Tables  {
  val keySpace: String = "JsonTypeSeqTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "work here but it fails- WE NEED TO FIX IT" in {
    class TestTable2 extends CassandraTable[TestTable2, TestRow] {
      def fromRow(r: Row): TestRow = {
        TestRow(key(r), optionA(r), classS(r), optionS(r), mapIntoClass(r))
      }
      object key extends PrimitiveColumn[String]
      object optionA extends OptionalPrimitiveColumn[Int]
      object classS extends JsonColumn[ClassSMap]
      object optionS extends JsonColumn[Option[ClassSMap]]
      object mapIntoClass extends JsonColumn[Map[String, ClassSMap]]
      val _key = key
    }

    val createTestTable =
      """|CREATE TABLE TestTable2(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        |optionS text,
        |mapIntoClass map<text,text>);
      """.stripMargin //        #|
    session.execute(createTestTable)

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
      .execute()

    rcp.successful {
      res => {
        try {
          Console.println("This works")
        } catch {
          case NonFatal(e) => fail(e.getMessage)
        }
      }
    }
  }
}
