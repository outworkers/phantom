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
package com.outworkers.phantom.streams.suites
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams._
import com.outworkers.phantom.tables.{TestDatabase, TimeUUIDRecord}
import com.outworkers.util.samplers._
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.tagobjects.Retryable
import org.scalatest.time.SpanSugar._

import scala.concurrent.Await

class PublisherIntegrationTest extends FlatSpec with StreamTest with TestImplicits with Eventually with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = 30.seconds, interval = 200.millis)

  it should "correctly consume the entire stream of items published from a Cassandra table" taggedAs Retryable in {
    val counter = new AtomicInteger(0)
    val generatorCount = 100
    val samples = genList[String](generatorCount).map(Opera)

    val chain = for {
      truncate <- StreamDatabase.operaTable.truncate().future()
      store <- StreamDatabase.operaTable.storeRecords(samples)
    } yield store

    Await.result(chain, 30.seconds)

    val publisher = StreamDatabase.operaTable.publisher

    publisher.subscribe(new Subscriber[Opera] {
      override def onError(t: Throwable): Unit = {
        fail(t)
      }

      override def onSubscribe(s: Subscription): Unit = {
        s.request(Long.MaxValue)
      }

      override def onComplete(): Unit = {
        info(s"Finished streaming, total count is ${counter.get()}")
      }

      override def onNext(t: Opera): Unit = {
        info(s"The current item is ${t.name}")
        info(s"The current count is ${counter.incrementAndGet()}")
      }
    })


    eventually {
      counter.get() shouldEqual generatorCount
    }
  }

  it should "allow streaming from a non top level select * statement" in {
    val counter = new AtomicLong(0)
    val generationSize = 100
    val user = gen[UUID]
    val samples = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user, id = UUIDs.timeBased()))

    val chain = for {
      store <- TestDatabase.timeuuidTable.storeRecords(samples)
      pub = TestDatabase.timeuuidTable.select.where(_.user eqs user).publisher()
    } yield pub

    whenReady(chain) { pub =>

      pub.subscribe(new Subscriber[TimeUUIDRecord] {
        override def onError(t: Throwable): Unit = fail(t)

        override def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

        override def onComplete(): Unit = {
          info(s"Finished streaming, total count is ${counter.get()}")
        }

        override def onNext(t: TimeUUIDRecord): Unit = {
          info(s"The current item is ${t.name}")
          info(s"The current count is ${counter.incrementAndGet()}")
        }
      })

      eventually {
        counter.get() shouldEqual generationSize
      }
    }
  }

  it should "allow streaming from a non top level select * statement with a modifier" in {
    val counter = new AtomicLong(0)
    val generationSize = 100
    val user = gen[UUID]
    val samples = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user, id = UUIDs.timeBased()))

    val chain = for {
      store <- TestDatabase.timeuuidTable.storeRecords(samples)
      pub = TestDatabase.timeuuidTable.select.where(_.user eqs user).publisher(_.setIdempotent(true))
    } yield pub

    whenReady(chain) { pub =>

      pub.subscribe(new Subscriber[TimeUUIDRecord] {
        override def onError(t: Throwable): Unit = fail(t)

        override def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

        override def onComplete(): Unit = {
          info(s"Finished streaming, total count is ${counter.get()}")
        }

        override def onNext(t: TimeUUIDRecord): Unit = {
          info(s"The current item is ${t.name}")
          info(s"The current count is ${counter.incrementAndGet()}")
        }
      })

      eventually {
        counter.get() shouldEqual generationSize
      }
    }
  }

  it should "allow streaming from top level prepared statements" in {
    val counter = new AtomicLong(0)
    val generationSize = 100
    val user = gen[UUID]
    val samples = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user, id = UUIDs.timeBased()))

    val chain = for {
      store <- TestDatabase.timeuuidTable.storeRecords(samples)
      query <- TestDatabase.timeuuidTable.select.where(_.user eqs ?).prepareAsync()
      pub = query.bind(user).publisher()
    } yield pub

    whenReady(chain) { pub =>

      pub.subscribe(new Subscriber[TimeUUIDRecord] {
        override def onError(t: Throwable): Unit = fail(t)

        override def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

        override def onComplete(): Unit = {
          info(s"Finished streaming, total count is ${counter.get()}")
        }

        override def onNext(t: TimeUUIDRecord): Unit = {
          info(s"The current item is ${t.name}")
          info(s"The current count is ${counter.incrementAndGet()}")
        }
      })

      eventually {
        counter.get() shouldEqual generationSize
      }
    }
  }

  it should "allow streaming from prepared statements with a modifier" in {
    val counter = new AtomicLong(0)
    val generationSize = 100
    val user = gen[UUID]
    val samples = genList[TimeUUIDRecord](generationSize).map(_.copy(user = user, id = UUIDs.timeBased()))

    val chain = for {
      store <- TestDatabase.timeuuidTable.storeRecords(samples)
      query <- TestDatabase.timeuuidTable.select.where(_.user eqs ?).prepareAsync()
      pub = query.bind(user).publisher()
    } yield pub

    whenReady(chain) { pub =>

      pub.subscribe(new Subscriber[TimeUUIDRecord] {
        override def onError(t: Throwable): Unit = fail(t)

        override def onSubscribe(s: Subscription): Unit = s.request(Long.MaxValue)

        override def onComplete(): Unit = {
          info(s"Finished streaming, total count is ${counter.get()}")
        }

        override def onNext(t: TimeUUIDRecord): Unit = {
          info(s"The current item is ${t.name}")
          info(s"The current count is ${counter.incrementAndGet()}")
        }
      })

      eventually {
        counter.get() shouldEqual generationSize
      }
    }
  }
}
