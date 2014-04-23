package com.newzly.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.{Sampler, BaseTest}
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.Implicits._
import com.newzly.phantom.thrift.ThriftTest
import com.newzly.util.testing.AsyncAssertionsHelper._


class ThriftSetOperationsTest extends BaseTest {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "thriftsetoperators"

  it should "add an item to a thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet add sample2).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2)
      }
    }
  }

  it should "add several items a thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet addAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2, sample3)
      }
    }
  }

  it should "remove one item from a thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet remove sample3).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2)
      }
    }
  }


  it should "remove several items from thrift set column" in {
    ThriftColumnTable.insertSchema

    val sample = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample2 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val sample3 = ThriftTest(
      Sampler.getARandomInteger(),
      Sampler.getAUniqueString,
      test = true
    )

    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet removeAll Set(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe Set(sample)
      }
    }
  }
}
