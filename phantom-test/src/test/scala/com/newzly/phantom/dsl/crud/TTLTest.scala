package com.newzly.phantom.dsl.crud

import java.net.InetAddress

import org.scalatest.{ Assertions, Matchers, Inside }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._

import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.{Primitive, BaseTest, Primitives}
import com.twitter.util.Duration

class TTLTest extends BaseTest with Matchers with Assertions with AsyncAssertions with Inside {
  val keySpace: String = "TTLTest"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "expire inserterd records" in {
    object Primitives extends Primitives {
      override def tableName = "PrimitivesTTLTest"
    }

    val row = Primitive("myStringInsert", 2.toLong, boolean = true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002
      ))

    val test = Primitives.create(_.pkey,
      _.long,
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi)
      .execute() flatMap {
      _ => Primitives.insert
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
      } flatMap {
          _ =>  Primitives.select.one
      }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get should be (row)
        Thread.sleep(Duration.fromSeconds(5).inMillis)
        val test2 = Primitives.select.one
        test2 successful {
          expired => {
            assert(expired.isEmpty)
          }
        }
      }
    }
  }
}
