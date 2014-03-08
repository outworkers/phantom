package com.newzly.phantom.dsl.specialized

import com.newzly.phantom.tables.{Recipe, Recipes}
import com.datastax.driver.core.utils.UUIDs

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.finagle.Implicits._
import com.newzly.phantom.helper.{Sampler, BaseTest}
import com.newzly.phantom.tables.ThriftColumnTable
import com.newzly.phantom.thrift.Implicits._
import com.newzly.phantom.thrift.ThriftTest
import com.newzly.util.finagle.AsyncAssertionsHelper._


class ThriftListOperationsTest extends BaseTest {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace = "thriftlistoperators"

  ignore should "append an item to a list" in {
    Recipes.insertSchema

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

    val id = UUIDs.timeBased()
    val insert = ThriftColumnTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .future()

    val operation = for {
      insertDone <- insert
      // update <- ThriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftSet append sample2).future()
      select <- ThriftColumnTable.select(_.thriftSet).where(_.id eqs sample.id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        Console.println(s"${items.mkString(" ")}")
        items.isDefined shouldBe true
        items.get shouldBe Set(sample, sample2)
      }
    }
  }
}
