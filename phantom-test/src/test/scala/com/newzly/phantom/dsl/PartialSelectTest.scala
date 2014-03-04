package com.newzly.phantom.dsl

import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import org.scalatest.Assertions
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.tables.{Primitives, Primitive, Article, Articles}

class PartialSelectTest extends BaseTest {
  val keySpace: String = "PartialSelect"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  "Select" should "work fine" in {
    val row = Primitive.sample
    Primitives.insertSchema(session)
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
        assert(r._2 === Some(row.long,row.boolean))
      }
    }
  }
}
