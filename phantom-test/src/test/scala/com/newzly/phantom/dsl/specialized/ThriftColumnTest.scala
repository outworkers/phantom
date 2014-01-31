package com.newzly.phantom.dsl.specialized

import org.scalatest.{ Assertions, FlatSpec, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.ThriftTest
import com.newzly.phantom.helper.{Sampler, BaseTest}
import com.twitter.util.{ Await, Duration }

class ThriftColumnTest extends FlatSpec with BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace = "thrift"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  it should "allow storing thrift columns" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(5, "test", test = true)

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .execute()

    insert.successful {
      result => {
        Console.println("Record inserted")
        val row = Await.result(ThriftColumnTable.select.one, Duration.fromSeconds(5))
        row.isEmpty shouldEqual false
        row.get.struct shouldEqual sample
      }
    }
  }

  it should "allow storing sequences of thrift columns" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(5, "test", test = true)
    val sampl2 = ThriftTest(6, "asasf", test = false)
    val l = sample :: sampl2 :: Nil

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSeq, l)
      .execute()

    insert.successful {
      result => {
        Console.println("Record inserted")
        val row = Await.result(ThriftColumnTable.select.one, Duration.fromSeconds(5))
        row.isEmpty shouldEqual false
        row.get.struct shouldEqual sample
        Console.println(row.get.list.mkString(""))
        row.get.list shouldEqual l
      }
    }
  }
}
