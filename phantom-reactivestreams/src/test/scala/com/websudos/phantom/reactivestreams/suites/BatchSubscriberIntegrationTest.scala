/*
 * Copyright 2013-2017 Outworkers, Limited.
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
package com.outworkers.phantom.reactivestreams.suites

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.outworkers.phantom.batch.BatchType
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.reactivestreams._
import com.outworkers.phantom.reactivestreams.suites.iteratee.OperaPublisher
import org.scalatest.{FlatSpec, Retries}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.tagobjects.Retryable

import scala.concurrent.Await
import scala.concurrent.duration._

class BatchSubscriberIntegrationTest extends FlatSpec with StreamTest with ScalaFutures with Retries {

  implicit val defaultPatience = PatienceConfig(timeout = 10.seconds, interval = 50.millis)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(StreamDatabase.autotruncate().future(), 5.seconds)
  }

  it should "persist all data" taggedAs Retryable in {
    val completionLatch = new CountDownLatch(1)

    val subscriber = StreamDatabase.operaTable.subscriber(
      batchSize = 2,
      concurrentRequests = 2,
      batchType = BatchType.Unlogged,
      flushInterval = None,
      completionFn = () => completionLatch.countDown()
    )

    OperaPublisher.subscribe(subscriber)

    completionLatch.await(5, TimeUnit.SECONDS)

    val chain = for {
      count <- StreamDatabase.operaTable.select.count().one()
    } yield count

    whenReady(chain) {
      res => res.value shouldEqual OperaData.operas.length
    }

  }

}

