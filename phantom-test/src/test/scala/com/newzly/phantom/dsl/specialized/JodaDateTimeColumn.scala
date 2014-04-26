package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.tables.{ JodaRow, PrimitivesJoda }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class JodaDateTimeColumn extends BaseTest {
  val keySpace = "joda_columns_test"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      PrimitivesJoda.insertSchema()
    }
  }

  it should "correctly insert and extract a JodaTime date" in {
    val row = JodaRow.sample
    PrimitivesJoda.insertSchema()
    val w =
        PrimitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.int)
          .value(_.timestamp, row.bi)
          .future() flatMap  {
            _ => PrimitivesJoda.select.one
          }

    w successful {
      res => res.get shouldEqual row
    }
  }
}
