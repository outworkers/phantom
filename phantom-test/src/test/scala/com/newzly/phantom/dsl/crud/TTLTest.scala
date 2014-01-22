package com.newzly.phantom.dsl.crud

import java.net.InetAddress
import scala.collection.breakOut
import org.scalatest.{ Assertions, Matchers, Inside }
import org.scalatest.concurrent.AsyncAssertions

import com.datastax.driver.core.Session
import com.newzly.phantom.dsl.BaseTest
import com.newzly.phantom.helper.{ AsyncAssertionsHelper, Tables }
import com.twitter.util.Duration

class TTLTest extends BaseTest with Matchers with Tables with Assertions with AsyncAssertions with Inside {

  implicit val session: Session = cassandraSession
  import AsyncAssertionsHelper._
  it should "expire inserterd records" in {
    val row = Primitive("myStringInsert", 2.toLong, boolean = true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002
      ))

    val l = List.empty[String]
    l.view.map(_ + "2")(breakOut)

    val rcp = Primitives.insert
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
      .ttl(Duration.fromSeconds(5))
      .execute()

    rcp.successful {
      result => {
        val test = Primitives.select.one
        test.successful {
          record => {
            !record.isEmpty shouldEqual false
            record.get should be (row)
            Thread.sleep(Duration.fromSeconds(5).inMillis)
            val test2 = Primitives.select.one
            test.successful {
              deletedRecord => {
                assert(deletedRecord.isEmpty)
              }
            }
          }
        }
      }
    }


  }
}
