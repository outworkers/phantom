package com.newzly.phantom.dsl.ordering

import scala.collection.JavaConverters._
import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ TimeSeriesRecord, TimeSeriesTable }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest
import com.newzly.phantom.iteratee.Iteratee

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
    val recordList = List.range(0, 5).map {
      res => {
        Thread.sleep(2500L)
        TimeSeriesRecord.sample
      }
    }

    val batch = recordList.foldLeft(BatchStatement()) {
      (b, record) => {
        b.add(TimeSeriesTable.insert
          .value(_.id, record.id)
          .value(_.name, record.name)
          .value(_.timestamp, record.timestamp))
      }
    }

    val chain = for {
      insert <- batch.future()
      select <- TimeSeriesTable.select.fetchEnumerator() flatMap {
        res => res run Iteratee.chunks()
      }
    } yield select


    chain.successful {
      res => {
        val ts = recordList.map(_.timestamp.getSecondOfDay)
        ts.sorted shouldEqual ts

        val set = session.execute(TimeSeriesTable.select.limit(5).qb.getQueryString).all()
        Console.println(set.asScala)
        val mapped = set.asScala.toList.map(TimeSeriesTable.fromRow).map(_.timestamp.getSecondOfDay)
        mapped shouldEqual ts

        val manual = set.asScala.map(_.getDate("timestamp").getSeconds)

        Console.println(manual)

        Console.println(ts.mkString("\n"))

        Console.println(res.map(_.timestamp.getSecondOfDay))
        //recordList.map(_.name) shouldEqual res.map(_.name)
      }
    }
  }
}
