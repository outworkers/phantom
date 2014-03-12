package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ JodaRow, PrimitivesJoda }

class JodaDateTimeColumn extends BaseTest {
  val keySpace = "UpdateTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "work fine" in {
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
      case res => assert(res.get === row)
    }
  }
}
