package com.newzly.phantom.dsl.specialized

import org.joda.time.DateTime
import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ JodaRow, PrimitivesJoda }

class JodaDateTimeColumn extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "UpdateTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "work fine" in {
    val row = JodaRow.sample

    val w = PrimitivesJoda.create(_.pkey,_.int,_.bi).execute() flatMap {
      _ => {
        PrimitivesJoda.insert.value(_.pkey,"w").value(_.int,1)
          .value(_.bi, row.bi).execute()
        }
      } flatMap  {
        _ => PrimitivesJoda.select.one
      }

    w successful {
      case res => assert(res.get === row)
    }
  }
}
