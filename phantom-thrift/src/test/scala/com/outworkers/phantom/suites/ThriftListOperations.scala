/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.suites

import com.outworkers.phantom.finagle._
import com.outworkers.phantom.tables.{ThriftDatabase, ThriftRecord}
import com.outworkers.util.samplers._
import com.outworkers.util.testing.twitter._
import org.scalatest.FlatSpec

class ThriftListOperations extends FlatSpec with ThriftTestSuite with TwitterFutures {

  it should "prepend an item to a thrift list column" in {
    val sample = gen[ThriftRecord]
    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable
        .update.where(_.id eqs sample.id)
        .modify(_.thriftList prepend sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual (sample2 :: sample.thriftList)
    }
  }

  it should "prepend an item to a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]
    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable
        .update.where(_.id eqs sample.id)
        .modify(_.thriftList prepend sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual (sample2 :: sample.thriftList)
    }
  }

  it should "prepend several items to a thrift list column" in {
    val sample = gen[ThriftRecord]

    val appendable = genList[ThriftTest]()

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList prepend appendable)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual prependedValues ::: sample.thriftList
    }
  }

  it should "prepend several items to a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val appendable = genList[ThriftTest]()

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList prepend appendable)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual prependedValues ::: sample.thriftList
    }
  }

  it should "append an item to a thrift list column" in {
    val sample = gen[ThriftRecord]
    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList append sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual sample.thriftList :+ sample2
    }
  }

  it should "append an item to a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]
    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList append sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual sample.thriftList :+ sample2
    }
  }

  it should "append several items to a thrift list column" in {
    val sample = gen[ThriftRecord]
    val sample2 = genList[ThriftTest]()

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList append sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList ::: sample2)
    }
  }

  it should "append several items to a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]
    val sample2 = genList[ThriftTest]()

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList append sample2)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList ::: sample2)
    }
  }

  it should "remove an item from a thrift list column" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      _ <- thriftDb.thriftColumnTable.store(sample).future
      update <- thriftDb.thriftColumnTable
        .update.where(_.id eqs sample.id)
        .modify(_.thriftList discard sample2)
        .future()
      select <- thriftDb.thriftColumnTable
        .select(_.thriftList)
        .where(_.id eqs sample.id)
        .one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList diff List(sample2))
    }
  }

  it should "remove an item from a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      _ <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable
        .update.where(_.id eqs sample.id)
        .modify(_.thriftList discard sample2)
        .future()
      select <- thriftDb.thriftColumnTable
        .select(_.thriftList)
        .where(_.id eqs sample.id)
        .one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList diff List(sample2))
    }
  }

  it should "remove several items from a thrift list column" in {
    val sample = gen[ThriftRecord]

    val removables = genList[ThriftTest]()

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id)
        .modify(_.thriftList discard removables)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList diff removables)
    }
  }

  it should "remove several items from a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val removables = genList[ThriftTest]()

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id)
        .modify(_.thriftList discard removables)
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value shouldEqual (sample.thriftList diff removables)
    }
  }

  it should "set an index to a given value" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList setIdx(0, sample2))
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value.isDefinedAt(2) shouldEqual true
      items.value should contain (sample2)
    }
  }

  it should "set an index to a given value with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update
        .where(_.id eqs sample.id)
        .modify(_.thriftList setIdx(0, sample2))
        .future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value should contain (sample2)
    }
  }

  it should "set a non-zero index to a given value" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftList setIdx(2, sample2)).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    whenReady(operation) { items =>
      items shouldBe defined
      items.value should contain (sample2)
    }
  }

  it should "set a non-zero index to a given value with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val sample2 = gen[ThriftTest]

    val operation = for {
      insertDone <- thriftDb.thriftColumnTable.store(sample).future()
      update <- thriftDb.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftList setIdx(2, sample2)).future()
      select <- thriftDb.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one()
    } yield select

    whenReady(operation.asScala) { items =>
      items shouldBe defined
      items.value should contain (sample2)
    }
  }
}
