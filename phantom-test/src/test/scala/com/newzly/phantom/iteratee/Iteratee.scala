package com.newzly.phantom.iteratee


import com.newzly.phantom.helper.BaseTest
import org.scalatest.{Assertions, Matchers}
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.tables.{Primitives, Primitive, PrimitivesJoda, JodaRow}
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.batch.BatchStatement
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await

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
      b <- batch.future()
      r<- PlayIteratee(PrimitivesJoda.select.future()).result
    } yield (r)


    Await.result(w,5.minutes) match  {
      case res =>
        //println(res)
        info(res.size.toString)
        assert(rows.size===res.size)

    }

  }
  it should "get mapResult fine" in {
    Primitives.insertSchema(session)
    val rows = for (i <- (1 to 20000)) yield  Primitive.sample
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
      b <- batch.future()
      all <- Primitives.select.future()
    } yield (all)

    val m = PlayIteratee(w)
          .resultMap {x => {
          assert(rows.contains(Primitives.fromRow(x)))
        }}

    println("first")
    Await.result(m,5.minutes) match  {
      case _ => info("ok")
    }

  }
}
