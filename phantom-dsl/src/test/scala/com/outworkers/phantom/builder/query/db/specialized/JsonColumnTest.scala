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
package com.outworkers.phantom.builder.query.db.specialized

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.testing._

class JsonColumnTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.jsonTable.insertSchema()
  }

  it should "allow storing a JSON record" in {
    val sample = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
    } yield select

    chain.successful {
      res => {
        res.value shouldEqual sample
      }
    }
  }

  it should "allow updating a JSON record" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value.json shouldEqual sample.json
        updated.value.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value shouldEqual sample
        updated.value.jsonList.headOption.value shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a Set of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- TestDatabase.jsonTable.store(sample).future()
      select <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one
      update <- TestDatabase.jsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).future()
      select2 <- TestDatabase.jsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      case (initial, updated) => {
        initial.value shouldEqual sample
        updated.value.jsonSet should contain (sample2.json)
      }
    }
  }
}
