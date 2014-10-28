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
package com.websudos.phantom.dsl.specialized

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.phantom.tables._
import com.websudos.util.testing._

class CounterColumnTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    CounterTableTest.insertSchema()
  }


  it should "increment counter values by 1" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment()).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 1
      }
    }
  }

  it should "increment counter values by 1 with Twitter Futures" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).execute()
      select <- CounterTableTest.select.where(_.id eqs sample.id).get
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment()).execute()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).get
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 1
      }
    }
  }


  it should "allow selecting a counter" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 500).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment()).future()
      select2 <- CounterTableTest.select(_.count_entries).where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 500
        result._2.isEmpty shouldEqual false
        result._2.get shouldEqual 501
      }
    }
  }

  it should "allow selecting a counter with Twitter Futures" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 500).execute()
      select <- CounterTableTest.select.where(_.id eqs sample.id).get
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment()).execute()
      select2 <- CounterTableTest.select(_.count_entries).where(_.id eqs sample.id).get
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 500
        result._2.isEmpty shouldEqual false
        result._2.get shouldEqual 501
      }
    }
  }

  it should "increment counter values by a given value" in {
    val sample = gen[CounterRecord]
    val diff = 200L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment diff).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual diff
      }
    }
  }

  it should "increment counter values by a given value with Twitter Futures" in {
    val sample = gen[CounterRecord]
    val diff = 200L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 0L).execute()
      select <- CounterTableTest.select.where(_.id eqs sample.id).get
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment diff).execute()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).get
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 0L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual diff
      }
    }
  }

  it should "decrement counter values by 1" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr1 <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 1L).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one()
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement()).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one()
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 1L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 0L
      }
    }
  }

  it should "decrement counter values by 1 with Twitter Futures" in {
    val sample = gen[CounterRecord]

    val chain = for {
      incr1 <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment 1L).execute()
      select <- CounterTableTest.select.where(_.id eqs sample.id).get()
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement()).execute()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).get()
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._1.get.count shouldEqual 1L
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual 0L
      }
    }
  }

  it should "decrement counter values by a given value" in {
    val sample = gen[CounterRecord]
    val diff = 200L
    val initial = 500L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment initial).future()
      select <- CounterTableTest.select.where(_.id eqs sample.id).one
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement diff).future()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).one
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual (initial - diff)
      }
    }
  }

  it should "decrement counter values by a given value with Twitter Futures" in {
    val sample = gen[CounterRecord]
    val diff = 200L
    val initial = 500L

    val chain = for {
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries increment initial).execute()
      select <- CounterTableTest.select.where(_.id eqs sample.id).get
      incr <-  CounterTableTest.update.where(_.id eqs sample.id).modify(_.count_entries decrement diff).execute()
      select2 <- CounterTableTest.select.where(_.id eqs sample.id).get
    } yield (select, select2)


    chain.successful {
      result => {
        result._1.isEmpty shouldEqual false
        result._2.isEmpty shouldEqual false
        result._2.get.count shouldEqual (initial - diff)
      }
    }
  }
}
