package com.newzly.phantom.dsl.crud

import java.net.InetAddress
import org.scalatest.{Assertions, Matchers}
import org.scalatest.concurrent.AsyncAssertions

import com.datastax.driver.core.Session

import com.newzly.phantom.dsl.BaseTest
import com.newzly.phantom.helper.Tables
import com.newzly.phantom.helper.AsyncAssertionsHelper

import com.twitter.util.Future



class DeleteTest extends BaseTest with Matchers with Tables with Assertions with AsyncAssertions {
  implicit val session: Session = cassandraSession
  import AsyncAssertionsHelper._

  "Delete" should "work fine, when deleting the whole row" in {

    val row = Primitive("myString", 2.toLong, boolean = true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002))
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

    Primitives.select.fetch successful {
      case res => {
        Console.println(res)
        assert(res contains row)
      }
    }

    val del = Primitives.delete.where(_.pkey eqs "myString")
    del.execute().sync()

    val recipeF2: Future[Option[Primitive]] = Primitives.select.where(_.pkey eqs "myString").one

    recipeF2 successful {
      case res => assert(res.isEmpty)
    }
  }

}
