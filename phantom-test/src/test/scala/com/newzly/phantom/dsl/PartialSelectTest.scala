package com.newzly.phantom.dsl

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitives, Primitive }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest


class PartialSelectTest extends BaseTest {

  val keySpace: String = "PartialSelect"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      Primitives.insertSchema()
    }
  }

  "Partially selecting 2 fields" should "work fine" in {
    val row = Primitive.sample
    val rcp =  Primitives.insert
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        for {
          a <- Primitives.select(_.pkey).fetch
          b <- Primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
        } yield (a, b)

      }
    }

    rcp successful {
      r => {
        assert(r._1 === List(row.pkey))
        assert(r._2 === Some(Tuple2(row.long, row.boolean)))
      }
    }
  }
}
