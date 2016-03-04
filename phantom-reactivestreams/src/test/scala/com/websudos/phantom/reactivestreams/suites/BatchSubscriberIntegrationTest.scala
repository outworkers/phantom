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

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.websudos.phantom.batch.BatchType
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._
import org.reactivestreams.{Publisher, Subscriber, Subscription}
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.Await
import scala.concurrent.duration._

class BatchSubscriberIntegrationTest extends FlatSpec with StreamTest with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = 10.seconds, interval = 50.millis)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(StreamDatabase.autotruncate().future(), 5.seconds)
  }

  it should "persist all data" in {
    val completionLatch = new CountDownLatch(1)

    val subscriber = StreamDatabase.operaTable.subscriber(
      2,
      2,
      BatchType.Unlogged,
      None,
      () => completionLatch.countDown()
    )

    OperaPublisher.subscribe(subscriber)

    completionLatch.await(5, TimeUnit.SECONDS)

    val chain = for {
      count <- StreamDatabase.operaTable.select.count().one()
    } yield count


    whenReady(chain) {
      res => {
        res.value shouldEqual OperaData.operas.length
      }
    }

  }

}

object OperaPublisher extends Publisher[Opera] {

  override def subscribe(s: Subscriber[_ >: Opera]): Unit = {
    var remaining = OperaData.operas

    s.onSubscribe(new Subscription {
      override def cancel(): Unit = ()

      override def request(l: Long): Unit = {

        remaining.take(l.toInt).foreach(s.onNext)

        remaining = remaining.drop(l.toInt)

        if (remaining.isEmpty) {
          s.onComplete()
        }
      }
    })
  }

}