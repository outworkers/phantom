package com.newzly.phantom.dsl

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.Matchers
import org.scalatest.time.SpanSugar._

import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper._
import com.twitter.util.NonFatal
import com.newzly.phantom.tables.{TestRow2, TestTable2}

class JsonColumnTest extends BaseTest with Matchers  {
  val keySpace: String = "JsonTypeSeqTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "correctly serialize and store complex structures" in {
    val createTestTable =
      """|CREATE TABLE TestTable2(
        |key text PRIMARY KEY,
        |optionA int,
        |classS text,
        |optionS text,
        |mapIntoClass map<text,text>);
      """.stripMargin //        #|

    session.execute(createTestTable)

    val row = TestRow2.sample

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
