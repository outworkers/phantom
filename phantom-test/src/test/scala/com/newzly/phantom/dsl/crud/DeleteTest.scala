package com.newzly.phantom.dsl.crud

import java.net.InetAddress
import scala.concurrent.Future
import com.newzly.phantom.dsl.BaseTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers
import com.newzly.phantom.helper.Tables
import com.datastax.driver.core.Session
import scala.concurrent.ExecutionContext.Implicits.global

class DeleteTest extends BaseTest with ScalaFutures with Matchers with Tables{
  implicit val session: Session = cassandraSession

  "Delete" should "work fine, when deleting the whole row" in {

    val row = Primitive("myString", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
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
    rcp.execute().sync()
    assert(Primitives.select.fetch.sync() contains row)

    val del = Primitives.delete.where(_.pkey eqs "myString")
    del.execute().sync()

    val recipeF2: Future[Option[Primitive]] = Primitives.select.where(_.pkey eqs "myString").one
    val rowFromDb = recipeF2.sync()
    assert(rowFromDb.isEmpty)
  }

}
