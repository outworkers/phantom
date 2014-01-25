package com.newzly.phantom.dsl

import org.scalatest.Matchers
import org.scalatest.time.SpanSugar._
import org.scalatest.concurrent.PatienceConfiguration

import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.{BaseTest => MyBaseTest, TableHelper, SimpleMapOfStringsClass, TestRow}
import com.twitter.util.NonFatal

class JsonColumnTest extends MyBaseTest  with Matchers  {
  val keySpace: String = "JsonTypeSeqTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "correctly serialize and store complex structures" in {
    class TestTable2 extends CassandraTable[TestTable2, TestRow] {
      def fromRow(r: Row): TestRow = {
        TestRow(key(r), optionA(r), classS(r), optionS(r), mapIntoClass(r))
      }
      object key extends PrimitiveColumn[String]
      object optionA extends OptionalPrimitiveColumn[Int]
      object classS extends JsonColumn[SimpleMapOfStringsClass]
      object optionS extends JsonColumn[Option[SimpleMapOfStringsClass]]
      object mapIntoClass extends JsonColumn[Map[String, SimpleMapOfStringsClass]]
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



    object TestTable2 extends TestTable2 {
      override val tableName = "TestTable2"
    }

    val row = TableHelper.getAUniqueJsonTestRow

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
