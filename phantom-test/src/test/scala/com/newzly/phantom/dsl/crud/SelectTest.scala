package com.newzly.phantom.dsl.crud

import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ Primitive, Primitives }

class SelectTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace: String = "selectTest"

  "Select" should "work fine" in {
    val row = Primitive.sample
    val rcp = Primitives.create.schema()
      .execute() flatMap { _ => Primitives.insert
        .value(_.pkey, row.pkey)
        .value(_.long, row.long)
        .value(_.boolean, row.boolean)
        .value(_.bDecimal, row.bDecimal)
        .value(_.double, row.double)
        .value(_.float, row.float)
        .value(_.inet, row.inet)
        .value(_.int, row.int)
        .value(_.date, row.date)
        .value(_.uuid, row.uuid)
        .value(_.bi, row.bi).execute() flatMap {
        _ => {
          for {
            a <- Primitives.select.fetch
            b <- Primitives.select.where(_.pkey eqs "1").one
          } yield (a contains row, b.get == row)

        }
      }
    }
    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
      }
    }
  }
}
