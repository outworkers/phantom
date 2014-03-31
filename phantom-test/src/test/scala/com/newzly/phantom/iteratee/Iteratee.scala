package com.newzly.phantom.iteratee


import com.newzly.phantom.helper.BaseTest
import org.scalatest.{Assertions, Matchers}
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import com.newzly.phantom.tables.{Primitives, Primitive, PrimitivesJoda, JodaRow}
import org.scalatest.time.SpanSugar._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.batch.BatchStatement
import java.util.concurrent.atomic.AtomicInteger


class IterateeTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "IterateeTestSpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(2 minutes)

  it should "get result fine" in {
    PrimitivesJoda.insertSchema()

    val rows = for (i <- 1 to 1000) yield  JodaRow.sample
    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
      val statement = PrimitivesJoda.insert
        .value(_.pkey, row.pkey)
        .value(_.intColumn, row.int)
        .value(_.timestamp, row.bi)
      b.add(statement)
    })

    val w = batch.future() flatMap (_ => PrimitivesJoda.select.setFetchSize(100).fetchEnumerator)
    w successful {
      en => {
        val result = en run Iteratee.collect()
        result successful {
          seqR =>
            for (row <- seqR)
              assert(rows.contains(row))
            assert(seqR.size === rows.size)
        }
      }
    }
  }

  it should "get mapResult fine" in {
    Primitives.insertSchema()
    val rows = for (i <- 1 to 2000) yield  Primitive.sample
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
      all <- Primitives.select.fetchEnumerator
    } yield all
    val counter: AtomicInteger = new AtomicInteger(0)
    val m = w flatMap {
      en => en run Iteratee.forEach(x => {counter.incrementAndGet(); assert(rows.contains(x))})
    }

    m successful {
      _ =>
        assert(counter.intValue()===rows.size)
    }
  }

  it should "get a slice of the iterator" in {
    Primitives.insertSchema()
    val rows = for (i <- 1 to 100) yield  Primitive.sample
    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
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
      all <- Primitives.select.fetchEnumerator
    } yield all

    val m = w flatMap {
      en => en run Iteratee.slice(10, 15)
    }

    m successful {
      res =>
        res.toIndexedSeq shouldBe rows.slice(10, 15)
    }
  }

  it should "drop records from the iterator" in {
    Primitives.insertSchema()
    val rows = for (i <- 1 to 100) yield  Primitive.sample
    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
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
      all <- Primitives.select.fetchEnumerator
    } yield all

    val m = w flatMap {
      en => en run Iteratee.drop(10)
    }

    m successful {
      res =>
        res.toIndexedSeq shouldBe rows.drop(10)
    }
  }

  it should "take records from the iterator" in {
    Primitives.insertSchema()
    val rows = for (i <- 1 to 100) yield  Primitive.sample
    val batch = rows.foldLeft(new BatchStatement())((b, row) => {
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
      all <- Primitives.select.fetchEnumerator
    } yield all

    val m = w flatMap {
      en => en run Iteratee.take(10)
    }

    m successful {
      res =>
        res.toIndexedSeq shouldBe rows.take(10)
    }
  }

}
