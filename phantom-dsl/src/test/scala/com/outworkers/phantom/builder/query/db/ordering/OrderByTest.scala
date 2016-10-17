/*
 * Copyright 2013-2016 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
