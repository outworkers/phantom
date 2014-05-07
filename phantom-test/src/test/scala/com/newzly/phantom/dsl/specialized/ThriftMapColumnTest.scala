package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.ThriftTest
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class ThriftMapColumnTest extends BaseTest {

  val keySpace = "thrift_map_operators"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      ThriftColumnTable.insertSchema
    }
  }

  it should "put an item to a thrift map column" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val map = Map("first" -> sample)
    val toAdd = "second" -> sample2
    val expected = map + toAdd


    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)

      .future()


    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap put toAdd).future()
      select <- ThriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe expected
      }
    }
  }

  it should "put an item to a thrift map column with Twitter Futures" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val map = Map("first" -> sample)
    val toAdd = "second" -> sample2
    val expected = map + toAdd


    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)
      .execute()


    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap put toAdd).execute()
      select <- ThriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe expected
      }
    }
  }


  it should "put several items to a thrift map column" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val map = Map("first" -> sample)
    val toAdd = Map("second" -> sample2, "third" -> sample3)
    val expected = map ++ toAdd


    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)

      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap putAll toAdd).future()
      select <- ThriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe expected
      }
    }
  }

  it should "put several items to a thrift map column with Twitter Futures" in {
    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getARandomString,
      test = true
    )

    val map = Map("first" -> sample)
    val toAdd = Map("second" -> sample2, "third" -> sample3)
    val expected = map ++ toAdd


    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .value(_.thriftMap, map)
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftMap putAll toAdd).execute()
      select <- ThriftColumnTable.select(_.thriftMap).where(_.id eqs sample.id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe expected
      }
    }
  }
}
