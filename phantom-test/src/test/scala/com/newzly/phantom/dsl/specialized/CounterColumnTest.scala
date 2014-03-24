package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ CounterRecord, CounterTableTest }
import com.newzly.util.finagle.AsyncAssertionsHelper._

class CounterColumnTest extends BaseTest {
  val keySpace = "counter_column_test"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "increment counter values by 1" in {
    CounterTableTest.insertSchema()

    val sample = CounterRecord.sample

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment()).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 1
      }
    }
  }

  it should "increment counter values by a given value" in {
    CounterTableTest.insertSchema()
    val sample = CounterRecord.sample
    val diff = 200L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment diff).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual diff
      }
    }
  }

  it should "decrement counter values by 1" in {
    CounterTableTest.insertSchema()
    val sample = CounterRecord.sample

    val chain = for {
      incr1 <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 1L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one()
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement()).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one()
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 1L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 0L
      }
    }
  }

  it should "decrement counter values by a given value" in {
    CounterTableTest.insertSchema()
    val sample = CounterRecord.sample
    val diff = 200L
    val initial = 500L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment initial).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement diff).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual (initial - diff)
      }
    }
  }
}
