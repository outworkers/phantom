package com.newzly.phantom.iteratee


import com.newzly.phantom.helper.BaseTest
import org.scalatest.{Assertions, Matchers}
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.tables.{Primitives, Primitive, PrimitivesJoda, JodaRow}
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.batch.BatchStatement
import org.joda.time.DateTime

class IterateeTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "IterateeTestSpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 minutes)

  it should "get result fine" in {
    PrimitivesJoda.insertSchema(session)

    val rows = for (i <- (1 to 50000)) yield  JodaRow.sample
    val batch = rows.foldLeft(new BatchStatement())((b,row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val w = for {
      b <- batch.execute()
      all <- PrimitivesJoda.select.execute()
      result <- IResultSet(all).result
    } yield (result)

    w successful     {
      case res =>
        assert(rows.size===res.size)
        info(res.size.toString)
    }
  }
  it should "get mapResult fine" in {
    Primitives.insertSchema(session)
    val rows = for (i <- (1 to 5000)) yield  Primitive.sample
    val batch = rows.foldLeft(new BatchStatement())((b,row) => {
      val statement = Primitives.insert
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
        .value(_.bi, row.bi)
      b.add(statement)
    })

    val w = for {
      b <- batch.execute()
      all <- Primitives.select.execute()
    } yield (all)

    val m = w flatMap {
      all => { info("mmmm")
        IResultSet(all).mapResult {x => {
          Console.println(x)
          info("ppp")
          assert(rows.contains(x))
        }}
      }
    }
    m successful {
      case _ => info("ok")
    }

  }
}
