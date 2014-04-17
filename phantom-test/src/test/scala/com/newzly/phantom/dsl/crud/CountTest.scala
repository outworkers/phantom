package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ PrimitivesJoda, JodaRow }
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.batch.BatchStatement

class CountTest extends BaseTest {
  val keySpace: String = "counttests"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "correctly retrieve a count" in {
    PrimitivesJoda.insertSchema()
    val limit = 1000

    val rows = Iterator.fill(limit)(JodaRow.sample)

    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    }).future() flatMap {
      _ => PrimitivesJoda.count.one()
    }

    batch successful {
      res => {
        res.isDefined shouldBe true
        res.get.get shouldBe 1
      }
    }
  }

  it should "correctly retrieve a count in a Twitter future" in {
    PrimitivesJoda.insertSchema()
    val limit = 1000

    val rows = Iterator.fill(limit)(JodaRow.sample)

    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    }).execute() flatMap {
      _ => PrimitivesJoda.count.get()
    }

    batch successful {
      res => {
        res.isDefined shouldBe true
        res.get.get shouldBe 1
      }
    }
  }
}
