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
import com.outworkers.util.samplers._

class CounterColumnTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.counterTableTest.createSchema()
  }

  it should "+= counter values by 1" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 0).future()
      select <- database.counterTableTest.select.where(_.id eqs sample.id).one
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 1).future()
      select2 <- database.counterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    whenReady(chain) { case (res1, res2) =>
      res1.value.count shouldEqual 0
      res2.value.count shouldEqual 1
    }
  }

  it should "allow selecting a counter" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 500).future()
      select <- database.counterTableTest.select.where(_.id eqs sample.id).one
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 1).future()
      select2 <- database.counterTableTest.select(_.count_entries).where(_.id eqs sample.id).one
    } yield (select, select2)


    whenReady(chain) { case (res1, res2) =>
      res1.value.count shouldEqual 500
      res2.value shouldEqual 501
    }
  }

  it should "+= counter values by a given value" in {
    val sample = gen[CounterRecord]
    val diff = 200

    val chain = for {
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 0).future()
      select <- database.counterTableTest.select.where(_.id eqs sample.id).one
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += diff).future()
      select2 <- database.counterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    whenReady(chain) { case (res, res1) =>
      res.value.count shouldEqual 0
      res1.value.count shouldEqual diff
    }
  }

  it should "-= counter values by 1" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr1 <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += 1).future()
      select <- database.counterTableTest.select.where(_.id eqs sample.id).one()
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries -= 1).future()
      select2 <- database.counterTableTest.select.where(_.id eqs sample.id).one()
    } yield (select, select2)


    whenReady(chain) { case (res, res1) =>
      res.value.count shouldEqual 1
      res1.value.count shouldEqual 0
    }
  }

  it should "-= counter values by a given value" in {
    val sample = gen[CounterRecord]
    val diff = 200
    val initial = 500

    val chain = for {
      incr <-  database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries += initial).future()
      incr <- database.counterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries -= diff).future()
      select2 <- database.counterTableTest.select.where(_.id eqs sample.id).one
    } yield select2


    whenReady(chain) { res =>
      res.value.count shouldEqual (initial - diff)
    }
  }
}
