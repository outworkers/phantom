package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.ThriftTest

class CounterColumnTest extends BaseTest {
  val keySpace = "counter_column_test"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "insert the counter value" in {
    ThriftColumnTable.insertSchema()
    val sample = ThriftTest(5, "test", test = true)

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.count_entries, 0L)
      .future() flatMap {
      _ => ThriftColumnTable.select.one
    }

    insert.successful {
      result => {
        result.isEmpty shouldEqual false
        result.get.struct shouldEqual sample
        result.get.count shouldEqual 0L
      }
    }
  }

  it should "increment counter values by 1" in {
    ThriftColumnTable.insertSchema()
    val sample = ThriftTest(5, "test", test = true)
    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.count_entries, 0L)
      .future()

    val chain = for {
      ins <- insert
      select <- ThriftColumnTable.select.where(_.id eqs sample.id).one
      incr <-  ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.count_entries increment()).future()
      select2 <- ThriftColumnTable.select.where(_.id eqs sample.id).one
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
    ThriftColumnTable.insertSchema()
    val sample = ThriftTest(5, "test", test = true)
    val diff = 200L
    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.count_entries, 0L)
      .future()

    val chain = for {
      ins <- insert
      select <- ThriftColumnTable.select.where(_.id eqs sample.id).one
      incr <-  ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.count_entries increment diff).future()
      select2 <- ThriftColumnTable.select.where(_.id eqs sample.id).one
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
    ThriftColumnTable.insertSchema()
    val sample = ThriftTest(5, "test", test = true)
    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.count_entries, 1L)
      .future()

    val chain = for {
      ins <- insert
      select <- ThriftColumnTable.select.where(_.id eqs sample.id).one
      incr <-  ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.count_entries decrement()).future()
      select2 <- ThriftColumnTable.select.where(_.id eqs sample.id).one
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
    ThriftColumnTable.insertSchema()
    val sample = ThriftTest(5, "test", test = true)
    val diff = 200L
    val initial = 500L
    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.count_entries, initial)
      .future()

    val chain = for {
      ins <- insert
      select <- ThriftColumnTable.select.where(_.id eqs sample.id).one
      incr <-  ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.count_entries decrement diff).future()
      select2 <- ThriftColumnTable.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual initial
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual initial - diff
      }
    }
  }


}
