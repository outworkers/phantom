package com.newzly.phantom.dsl

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.Matchers
import org.scalatest.time.SpanSugar._

import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper._
import com.newzly.phantom.tables.{ TestRow2, TestTable2 }
import com.twitter.util.NonFatal

class JsonColumnTest extends BaseTest with Matchers  {
  val keySpace: String = "JsonTypeSeqTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "correctly serialize and store complex structures" in {
    TestTable2.insertSchema(session)

    val row = TestRow2.sample

    val rcp = TestTable2.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionalInt)
      .value(_.classS, row.simpleMapOfString)
      .value(_.optionS, row.optionalSimpleMapOfString)
      .value(_.mapIntoClass, row.mapOfStringToCaseClass)
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
