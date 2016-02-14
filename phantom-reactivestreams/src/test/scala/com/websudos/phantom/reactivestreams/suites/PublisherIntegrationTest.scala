/*
 * Copyright 2013-2015 Websudos, Limited.
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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.reactivestreams.suites

import java.util.concurrent.atomic.AtomicInteger

import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import com.websudos.util.testing._
import org.reactivestreams.{Subscriber, Subscription}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._

import scala.concurrent.Await

class PublisherIntegrationTest extends FlatSpec with StreamTest with TestImplicits with Eventually {

  implicit val defaultPatience = PatienceConfig(timeout = 10.seconds, interval = 50.millis)

  it should "correctly consume the entire stream of items published from a Cassandra table" in {
    val counter = new AtomicInteger(0)
    val generatorCount = 100
    val samples = genList[String](generatorCount).map(Opera)

    val chain = for {
      truncate <- StreamDatabase.operaTable.truncate().future()
      store <- samples.foldLeft(Batch.unlogged) {
        (acc, item) => {
          acc.add(StreamDatabase.operaTable.store(item))
        }
      } future()
    } yield store

    Await.result(chain, 10.seconds)

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
