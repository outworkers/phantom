package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.StaticTableTest
import com.newzly.util.finagle.AsyncAssertionsHelper._

class StaticColumnTest extends BaseTest {
  val keySpace = "static_columns_test"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  "Insert" should "use a static value for a static column" in {

    StaticTableTest.insertSchema()

    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val id2 = UUIDs.timeBased()
    val chain = for {
      // The first record holds the static value.
      insert <- StaticTableTest.insert.value(_.id, id).value(_.string, static).execute()
      insert2 <- StaticTableTest.insert.value(_.id, id2).execute()
      select <- StaticTableTest.select.where(_.id eqs id2).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get._2 shouldEqual static
      }
    }
  }

  "Updating a static column" should "update values in all rows" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java

    StaticTableTest.insertSchema()

    val id = UUIDs.timeBased()
    val static = "this_is_static"
    val static2 = "this_is_updated_static"
    val id2 = UUIDs.timeBased()
    val chain = for {

      // The first insert holds the first static value.
      insert <- StaticTableTest.insert.value(_.id, id).value(_.string, static).execute()

      // The second insert updates the static value
      insert2 <- StaticTableTest.insert.value(_.id, id2).value(_.string, static2).execute()

      // We query for the first record inserted.
      select <- StaticTableTest.select.where(_.id eqs id).get()
    } yield select

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        // The first record should hold the updated value.
        res.get._2 shouldEqual static2
      }
    }
  }

}
