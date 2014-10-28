/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.thrift

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.ThriftColumnTable
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class ThriftListOperations extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftColumnTable.insertSchema()
  }

  it should "prepend an item to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]
    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList prepend sample2).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample2, sample)
      }
    }
  }

  it should "prepend an item to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList prepend sample2).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample2, sample)
      }
    }
  }

  it should "prepend several items to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList prependAll toAppend).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample3, sample2, sample)
      }
    }
  }

  it should "prepend several items to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList prependAll toAppend).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample3, sample2, sample)
      }
    }
  }

  it should "append an item to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]
    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append sample2).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample, sample2)
      }
    }
  }

  it should "append an item to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList append sample2).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample, sample2)
      }
    }
  }

  it should "append several items to a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList appendAll toAppend).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample, sample2, sample3)
      }
    }
  }

  it should "append several items to a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val toAppend = List(sample2, sample3)

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList appendAll toAppend).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample, sample2, sample3)
      }
    }
  }

  it should "remove an item from a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discard sample2).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample)
      }
    }
  }

  it should "remove an item from a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discard sample2).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample)
      }
    }
  }

  it should "remove several items from a thrift list column" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discardAll List(sample2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample)
      }
    }
  }

  it should "remove several items from a thrift list column with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList discardAll List(sample2, sample3)).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get shouldEqual List(sample)
      }
    }
  }

  it should "set an index to a given value" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(0, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get(0) shouldEqual sample3
      }
    }
  }

  it should "set an index to a given value with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(0, sample3)).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get(0) shouldEqual sample3
      }
    }
  }

  it should "set a non-zero index to a given value" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .future()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(2, sample3)).future()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get(2) shouldEqual sample3
      }
    }
  }

  it should "set a non-zero index to a given value with Twitter Futures" in {
    val id = gen[UUID]

    val sample = gen[ThriftTest]

    val sample2 = gen[ThriftTest]

    val sample3 = gen[ThriftTest]

    val insert = ThriftColumnTable.insert
      .value(_.id, id)
      .value(_.name, sample.name)
      .value(_.ref, sample)
      .value(_.thriftSet, Set(sample))
      .value(_.thriftList, List(sample, sample2, sample3))
      .execute()

    val operation = for {
      insertDone <- insert
      update <- ThriftColumnTable.update.where(_.id eqs id).modify(_.thriftList setIdx(2, sample3)).execute()
      select <- ThriftColumnTable.select(_.thriftList).where(_.id eqs id).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldEqual true
        items.get(2) shouldEqual sample3
      }
    }
  }
}
