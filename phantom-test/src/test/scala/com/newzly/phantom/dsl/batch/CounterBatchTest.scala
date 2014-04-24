package com.newzly.phantom.dsl.batch

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ CounterTableTest, SecondaryCounterTable }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class CounterBatchTest extends BaseTest {
  val keySpace = "counter_batch_test"

  override def beforeAll(): Unit = {
    super.beforeAll()
    CounterTableTest.insertSchema()
    SecondaryCounterTable.insertSchema()
  }

  it should "create a batch query to perform several updates in a single table" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))

    val chain = for {
      batched <- ft.future()
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to perform several updates in a single table with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .execute()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to update counters in several tables" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .future()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).one()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).one()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 2500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 2500L
      }
    }
  }

  it should "create a batch query to update counters in several tables with Twitter Futures" in {
    val id = UUIDs.timeBased()
    val ft = CounterBatchStatement()
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(CounterTableTest.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .add(SecondaryCounterTable.update.where(_.id eqs id).modify(_.count_entries increment 500L))
      .execute()

    val chain = for {
      batched <- ft
      get <- CounterTableTest.select(_.count_entries).where(_.id eqs id).get()
      get2 <- SecondaryCounterTable.select(_.count_entries).where(_.id eqs id).get()
    } yield (get, get2)

    chain.successful {
      res => {
        info("The first counter select should return the record")
        res._1.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._1.get shouldEqual 2500L

        info("The second counter select should return the record")
        res._2.isDefined shouldEqual true
        info("and the counter value should match the sum of the increments")
        res._2.get shouldEqual 2500L
      }
    }
  }
}
