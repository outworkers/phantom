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

import com.outworkers.phantom.tables.{ThriftRecord, ThriftDatabase}
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.finagle._
import com.outworkers.phantom.tables.ThriftDatabase
import com.outworkers.util.testing._
import com.outworkers.util.testing.twitter._
import org.scalatest.FlatSpec
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class ThriftListOperations extends FlatSpec with ThriftTestSuite {

  it should "prepend an item to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable
        .update.where(_.id eqs id)
        .modify(_.thriftList prepend sample2)
        .future()
      select <- ThriftDatabase.thriftColumnTable
        .select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample2, sample)
    }
  }

  it should "prepend an item to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList prepend sample2).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample2, sample)
    }
  }

  it should "prepend several items to a thrift list column" in {
    val sample = gen[ThriftRecord]

    val appendable = genList[ThriftTest]()

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).future()
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftList prepend appendable).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).one
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual prependedValues ::: sample.thriftList
    }
  }

  it should "prepend several items to a thrift list column with Twitter Futures" in {
    val sample = gen[ThriftRecord]

    val appendable = genList[ThriftTest]()

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      insertDone <- ThriftDatabase.thriftColumnTable.store(sample).execute()
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs sample.id).modify(_.thriftList prepend appendable).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs sample.id).get
    } yield {
        select
      }

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual prependedValues ::: sample.thriftList
    }
  }

  it should "append an item to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]
    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append sample2).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample, sample2)
    }
  }

  it should "append an item to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append sample2).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample, sample2)
    }
  }

  it should "append several items to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append toAppend).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample, sample2, sample3)
    }
  }

  it should "append several items to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append toAppend).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample, sample2, sample3)
    }
  }

  it should "remove an item from a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discard sample2).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample)
    }
  }

  it should "remove an item from a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discard sample2).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample)
    }
  }

  it should "remove several items from a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discard List(sample2, sample3)).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample)
    }
  }

  it should "remove several items from a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id)
        .modify(_.thriftList discard List(sample2, sample3)).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value shouldEqual List(sample)
    }
  }

  it should "set an index to a given value" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(0, sample3)).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value.headOption.value shouldEqual sample3
    }
  }

  it should "set an index to a given value with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(0, sample3)).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value.drop(2).headOption.value shouldEqual sample3
    }
  }

  it should "set a non-zero index to a given value" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(2, sample3)).future()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful { items =>
      items.isDefined shouldEqual true
      items.value should contain (sample3)
    }
  }

  it should "set a non-zero index to a given value with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftDatabase.thriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftDatabase.thriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(2, sample3)).execute()
      select <- ThriftDatabase.thriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful { items =>
      items shouldBe defined
      items.value should contain (sample3)
    }
  }
}
