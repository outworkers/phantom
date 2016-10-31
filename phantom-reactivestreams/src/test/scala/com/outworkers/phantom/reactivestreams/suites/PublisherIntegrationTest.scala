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
package com.outworkers.phantom.reactivestreams.suites

import java.util.concurrent.atomic.AtomicInteger

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.reactivestreams._
import com.outworkers.util.testing._
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually
import org.scalatest.tagobjects.Retryable
import org.scalatest.time.SpanSugar._

import scala.concurrent.{Await, Future}

class PublisherIntegrationTest extends FlatSpec with StreamTest with TestImplicits with Eventually {

  implicit val defaultPatience = PatienceConfig(timeout = 30.seconds, interval = 200.millis)

  it should "correctly consume the entire stream of items published from a Cassandra table" taggedAs Retryable in {
    val counter = new AtomicInteger(0)
    val generatorCount = 100
    val samples = genList[String](generatorCount).map(Opera)

    val chain = for {
      truncate <- StreamDatabase.operaTable.truncate().future()
      store <- Future.sequence(samples.map(StreamDatabase.operaTable.store(_).future()))
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
    } (defaultPatience)
  }
}
