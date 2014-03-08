package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.finagle.Implicits._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.ThriftTest
import com.twitter.util.{ Await, Duration }

class ThriftColumnTest extends BaseTest {
  val keySpace = "thrift"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  it should "allow storing thrift columns" in {

    ThriftColumnTable.insertSchema

    val sample = ThriftTest(5, "test", test = true)

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .future() flatMap {
      _ => ThriftColumnTable.select.one
    }

    insert.successful {
      result => {
        Console.println("Record inserted")
        result.isEmpty shouldEqual false
        result.get.struct shouldEqual sample
      }
    }
  }

  it should "allow storing lists of thrift objects" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(5, "test", test = true)
    val sample2 = ThriftTest(6, "asasf", test = false)
    val sampleList = Set(sample, sample2)

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, sampleList)
      .future() flatMap {
      _ => ThriftColumnTable.select.one
    }

    insert.successful {
      result => {
        Console.println("Record inserted")
        result.isEmpty shouldEqual false
        result.get.struct shouldEqual sample
        Console.println(result.get.list.mkString(""))
        result.get.list shouldEqual sampleList
      }
    }
  }
}
