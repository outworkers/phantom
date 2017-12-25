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
package com.outworkers.phantom.builder.query.db.ordering

import java.util.UUID

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TimeUUIDRecord
import com.outworkers.util.samplers._

class OrderByTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.timeuuidTable.createSchema()
  }

  it should "store a series of records and retrieve them in the right order" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      get <- database.timeuuidTable.retrieve(user)
      desc <- database.timeuuidTable.retrieveDescending(user)
    } yield (get, desc)


    whenReady(chain) { case (asc, desc) =>
      val orderedAsc = records.sortWith((a, b) => { a.id.compareTo(b.id) <= 0 })

      info("The ascending results retrieved from the DB")
      info(asc.mkString("\n"))

      info("The ascending results expected")
      info(orderedAsc.mkString("\n"))

      asc should contain theSameElementsAs orderedAsc

      val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

      info("The descending results retrieved from the DB")
      info(desc.mkString("\n"))

      info("The descending results expected")
      info(orderedDesc.mkString("\n"))

      desc should contain theSameElementsAs orderedDesc
    }
  }


  it should "retrieve ordered records using where + Cassandra lte operator clause" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      results <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id lte now())
        .orderBy(_.id descending)
        .fetch()
    } yield results


    whenReady(chain) { results =>
      val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

      results should contain theSameElementsAs orderedDesc
    }
  }


  it should "retrieve ordered records using where + Cassandra <= operator clause" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      results <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id <= now())
        .orderBy(_.id descending)
        .fetch()
    } yield results


    whenReady(chain) { results =>
      val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

      results should contain theSameElementsAs orderedDesc
    }
  }


  it should "retrieve ordered records using where + Cassandra eqs operator clause" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      results <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id eqs now())
        .orderBy(_.id descending)
        .fetch()
    } yield results


    whenReady(chain) { results =>
      results shouldBe empty
    }
  }

  it should "retrieve ordered records using where + Cassandra lt operator clause" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      results <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id lt now())
        .orderBy(_.id descending)
        .fetch()
    } yield results


    whenReady(chain) { results =>
      val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

      results should contain theSameElementsAs orderedDesc
    }
  }

  it should "retrieve ordered records using where + Cassandra < operator clause" in {
    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map { rec =>
      rec.copy(user = user, id = UUIDs.timeBased())
    }

    val chain = for {
      _ <- database.timeuuidTable.storeRecords(records)
      results <- database.timeuuidTable.select
        .where(_.user eqs user)
        .and(_.id < now())
        .orderBy(_.id descending)
        .fetch()
    } yield results


    whenReady(chain) { results =>
      val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

      results should contain theSameElementsAs orderedDesc
    }
  }
}
