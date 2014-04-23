package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.util.testing.cassandra.BaseTest
import com.newzly.phantom.tables.{ Article, Articles }
import com.newzly.util.testing.AsyncAssertionsHelper._

class TruncateTest extends BaseTest {
  val keySpace: String = "truncate_test"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "truncate all records in a table" in {
    Articles.insertSchema()
    val article1 = Article.sample
    val article2 = Article.sample
    val article3 = Article.sample
    val article4 = Article.sample

    val result = for {
      i1 <- Articles.insert
        .value(_.name, article1.name).value(_.id, article1.id)
        .value(_.order_id, article1.order_id)
        .future()
      i2 <- Articles.insert
        .value(_.name, article2.name)
        .value(_.id, article2.id)
        .value(_.order_id, article2.order_id)
        .future()
      i3 <- Articles.insert
        .value(_.name, article3.name)
        .value(_.id, article3.id)
        .value(_.order_id, article3.order_id)
        .future()

      i4 <- Articles.insert
        .value(_.name, article4.name)
        .value(_.id, article4.id)
        .value(_.order_id, article4.order_id)
        .future()
      records <- Articles.select.fetch
      truncate <- Articles.truncate.future()
      records1 <- Articles.select.fetch
    } yield (records, records1)


    result successful {
      r => {
        r._1.size shouldEqual 4
        info (s"inserted exactly ${r._1.size} records")

        r._2.size shouldEqual 0
        info (s"got exactly ${r._2.size} records")
      }
    }
  }
}
