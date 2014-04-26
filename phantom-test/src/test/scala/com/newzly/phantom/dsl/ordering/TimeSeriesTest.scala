package com.newzly.phantom.dsl.ordering

import scala.concurrent.blocking
import org.joda.time.DateTime
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ TimeSeriesRecord, TimeSeriesTable }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class TimeSeriesTest extends BaseTest {
  val keySpace = "clustering_order_tests"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      TimeSeriesTable.insertSchema()
    }
  }

  it should "allow using naturally fetch the records in descending order for a descending clustering order" in {
    val recordList = List.fill(10)(TimeSeriesRecord.sample)

    val batch = recordList.foldLeft(BatchStatement()) {
      (b, record) => {
        Thread.sleep(200L)
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, new DateTime))
      }
    }

    val chain = for {
      insert <- batch.future()
      select <- TimeSeriesTable.select(_.name).limit(10).fetch()
    } yield select


    chain.successful {
      res => {
        val expected = recordList.map(_.name)
        Console.println(expected.reverse)
        Console.println()
        Console.println()
        Console.println(res)
        expected shouldEqual res
      }
    }
  }
}
