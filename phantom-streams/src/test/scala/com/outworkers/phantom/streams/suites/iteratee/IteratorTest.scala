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

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{TestDatabase, TimeUUIDRecord}
import com.outworkers.util.testing._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class IteratorTest extends BigTest with ScalaFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeuuidTable.insertSchema()
  }

  it should "correctly retrieve the right number of records using scala iterator" in {
    val generationSize = 100
    val user = gen[UUID]
    val rows = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user))

    val chain = for {
      store <- Future.sequence(rows.map(row => database.timeuuidTable.store(row).future()))
      iterator <- database.timeuuidTable.select.where(_.user eqs user).iterator()
    } yield iterator

    whenReady(chain) {
      res => {
        res.records.size shouldEqual generationSize
        res.records.forall(rows contains _)
      }
    }
  }

  ignore should "correctly paginate a query using an iterator" in {
    val generationSize = 100
    val fetchSize = generationSize / 2
    val user = gen[UUID]
    val rows = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user))

    val chain = for {
      store <- Future.sequence(rows.map(row => database.timeuuidTable.store(row).future()))
      firstHalf <- database.timeuuidTable.select.where(_.user eqs user).iterator(_.setFetchSize(fetchSize))
      secondHalf <- database.timeuuidTable.select.where(_.user eqs user).iterator(firstHalf.pagingState)
    } yield (firstHalf, secondHalf)

    whenReady(chain) {
      case (firstBatch, secondBatch) => {
        firstBatch.records.size shouldEqual fetchSize
        firstBatch.records.forall(rows contains _)

        secondBatch.records.size shouldEqual fetchSize
        secondBatch.records.forall(rows contains _)
      }
    }
  }
}
