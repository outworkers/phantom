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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.PrimitiveRecord
import com.outworkers.util.samplers._
import org.scalatest.{Outcome, Retries}
import org.scalatest.concurrent.Eventually
import org.scalatest.tagobjects.Retryable

import scala.concurrent.duration._

class TTLTest extends PhantomSuite with Eventually with Retries {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.primitives.createSchema()
  }

  override def withFixture(test: NoArgTest): Outcome = {
    if (isRetryable(test)) {
      withRetry(super.withFixture(test))
    } else {
      super.withFixture(test)
    }
  }

  private[this] val ttl = 2 seconds
  private[this] val granularity = 5 seconds

  it should "expire inserted records after TTL" taggedAs Retryable in {
    val row = gen[PrimitiveRecord]

    val chain = for {
      _ <- database.primitives.store(row).ttl(ttl).future()
      get <- database.primitives.select.where(_.pkey eqs row.pkey).one()
    } yield get

    whenReady(chain) { record =>
      record shouldEqual Some(row)
    }

    eventually(timeout(ttl + granularity)) {
      val futureRecord = database.primitives.select.where(_.pkey eqs row.pkey).one()
      whenReady(futureRecord) { record =>
        record shouldBe empty
      }
    }
  }

  it should "expire inserted records after TTL with prepared statement" taggedAs Retryable in {
    val row = gen[PrimitiveRecord]

    val fetchQuery = database.primitives.select
      .where(_.pkey eqs ?)
      .prepare()

    val insertQuery = database.primitives.insert
      .p_value(_.pkey, ?)
      .p_value(_.long, ?)
      .p_value(_.boolean, ?)
      .p_value(_.bDecimal, ?)
      .p_value(_.double, ?)
      .p_value(_.float, ?)
      .p_value(_.inet, ?)
      .p_value(_.int, ?)
      .p_value(_.date, ?)
      .p_value(_.uuid, ?)
      .p_value(_.bi, ?)
      .ttl(ttl)
      .prepare()

    val chain = for {
      _ <- insertQuery.bind(row).future()
      get <- fetchQuery.bind(row.pkey).one()
    } yield get

    whenReady(chain) { result =>
      result shouldEqual Some(row)
    }

    eventually(timeout(ttl + granularity)) {
      val futureResults = fetchQuery.bind(row.pkey).one()
      whenReady(futureResults) { results =>
        results.isEmpty shouldBe true
      }
    }
  }
}
