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
package com.outworkers.phantom.streams.suites.iteratee

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TimeUUIDRecord
import com.outworkers.util.samplers._
import org.scalatest.concurrent.ScalaFutures

class IteratorTest extends BigTest with ScalaFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeuuidTable.createSchema()
  }

  it should "correctly retrieve the right number of records using scala iterator" in {
    val generationSize = 100
    val user = gen[UUID]
    val rows = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user, id = UUIDs.timeBased()))

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(rows)
      iterator <- database.timeuuidTable.select.where(_.user eqs user).iterator()
    } yield iterator

    whenReady(chain) { res =>
      res.records.size shouldEqual generationSize
      res.records.forall(rows contains _)
    }
  }

  it should "correctly paginate a query using an iterator" in {
    val generationSize = 100
    val fetchSize = generationSize / 2
    val user = gen[UUID]

    val rows = genList[TimeUUIDRecord](generationSize).map {
      rec => rec.copy(user = user, id = UUIDs.timeBased())
    } sortBy(_.id) reverse

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(rows)
      count <- database.timeuuidTable.select.count().where(_.user eqs user).one()
      firstHalf <- database.timeuuidTable.select.where(_.user eqs user)
        .orderBy(_.id desc)
        .paginateRecord(_.setFetchSize(fetchSize))

      secondHalf <- database.timeuuidTable.select.where(_.user eqs user)
        .orderBy(_.id desc)
        .paginateRecord(firstHalf.pagingState)

    } yield (count, firstHalf, secondHalf)

    whenReady(chain) { case (count, firstBatch, secondBatch) =>
      count shouldBe defined
      count.value shouldEqual generationSize

      Option(firstBatch.pagingState) shouldBe defined
      firstBatch.state shouldBe defined
      firstBatch.records.size shouldEqual fetchSize
      firstBatch.records should contain theSameElementsAs (rows take fetchSize)

      secondBatch.records.size shouldEqual fetchSize
      secondBatch.records should contain theSameElementsAs (rows drop fetchSize)
    }
  }
}
