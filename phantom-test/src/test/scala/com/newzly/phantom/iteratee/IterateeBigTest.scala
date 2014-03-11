package com.newzly.phantom.iteratee

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.{ Await, Future }
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.batch.BatchStatement
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.tables.{ PrimitivesJoda, JodaRow }




class IterateeBigTest extends BigTest {
  val keySpace: String = "BigIterateeTestSpace"

  implicit val s: PatienceConfiguration.Timeout = timeout(12 minutes)

  it should "get result fine" in {
    PrimitivesJoda.insertSchema()
    val fs = for {
      step <- 1 to 100
      rows = Iterator.fill(10000)(JodaRow.sample)

      batch = rows.foldLeft(new BatchStatement())((b, row) => {
        val statement = PrimitivesJoda.insert
          .value(_.pkey, row.pkey)
          .value(_.intColumn, row.int)
          .value(_.timestamp, row.bi)
        b.add(statement)
      })
      w = batch.future()
      f = w map (_ => println(s"step $step has succeed") )
      r = Await.result(f, 200 seconds)
    } yield f map (_ => r)

    val combinedFuture = Future.sequence(fs) map {
      r => session.execute(s"select count(*) from ${PrimitivesJoda.tableName}")
    } map {
      rs =>  rs.one().getLong(0)
    }

    val counter: AtomicLong = new AtomicLong(0)
    val result = combinedFuture flatMap {
       rs => {
         info(s"done, inserted: $rs rows - start parsing")
         PrimitivesJoda.select.setFetchSize(10000).fetchEnumerator  flatMap (_ run Iteratee.forEach { r=> counter.incrementAndGet() })
       }
    }

    (result flatMap (_ => combinedFuture)) successful {
      r => {
        info(s"done, reading: ${counter.get}")
        assert(counter.get() === r)
      }
    }
  }
}
