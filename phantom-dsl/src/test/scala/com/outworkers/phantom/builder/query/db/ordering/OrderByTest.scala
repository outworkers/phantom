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
import com.outworkers.phantom.tables.{TestDatabase, TimeUUIDRecord}
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

class OrderByTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.timeuuidTable.insertSchema()
  }

  it should "store a series of records and retrieve them in the right order" in {

    val user = gen[UUID]

    info(s"Generating a list of records with the same partition key value: $user")
    val records = genList[TimeUUIDRecord]().map(_.copy(user = user))

    val chain = for {
      store <- Future.sequence(records.map(database.timeuuidTable.store(_).future()))
      get <- database.timeuuidTable.retrieve(user)
      desc <- database.timeuuidTable.retrieveDescending(user)
    } yield (get, desc)


    whenReady(chain) {
      case (asc, desc) => {
        val orderedAsc = records.sortWith((a, b) => { a.id.compareTo(b.id) <= 0 })

        info("The ascending results retrieved from the DB")
        info(asc.mkString("\n"))

        info("The ascending results expected")
        info(orderedAsc.mkString("\n"))

        asc shouldEqual orderedAsc

        val orderedDesc = records.sortWith((a, b) => { a.id.compareTo(b.id) >= 0 })

        info("The ascending results retrieved from the DB")
        info(desc.mkString("\n"))

        info("The ascending results expected")
        info(orderedDesc.mkString("\n"))

        desc shouldEqual orderedDesc

      }
    }
  }

}
