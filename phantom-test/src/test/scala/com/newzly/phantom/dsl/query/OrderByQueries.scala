package com.newzly.phantom.dsl.query

import scala.concurrent.blocking
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ TimeSeriesTable, TimeSeriesRecord }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class OrderByQueries extends BaseTest {
  val keySpace = "order_by_test"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      TimeSeriesTable.insertSchema()
    }
  }

}

