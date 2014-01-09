package com.newzly.phantom.dsl.crud

import com.newzly.phantom.dsl.BaseTest
import org.scalatest.Matchers
import com.newzly.phantom.helper.Tables
import com.datastax.driver.core.Session
import java.net.InetAddress
import com.twitter.util.Future

class SelectTest extends BaseTest with Matchers with Tables{

  implicit val session: Session = cassandraSession

  "Select" should "work fine" in {
    val row = Primitive("1", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
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
    assert(Primitives.select.fetch.sync() contains (row))

    val select1 = Primitives.select.where(_.pkey eqs "1").one.sync()
    assert(select1.get === row)
  }
}
