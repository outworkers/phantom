/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.outworkers.phantom.builder.batch.BatchType
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams._
import com.outworkers.phantom.streams.suites.iteratee.OperaPublisher
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.tagobjects.Retryable
import org.scalatest.{FlatSpec, Retries}

import scala.concurrent.Await
import scala.concurrent.duration._

class BatchSubscriberIntegrationTest extends FlatSpec with StreamTest with ScalaFutures with Retries {

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = 10.seconds, interval = 50.millis)

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _= Await.result(StreamDatabase.autotruncate().future(), 5.seconds)
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

    completionLatch.await(defaultPatience.timeout.millisPart, TimeUnit.MILLISECONDS)

    val chain = for {
      count <- StreamDatabase.operaTable.select.count().one()
    } yield count

    whenReady(chain) {
      res => res.value shouldEqual OperaData.operas.length
    }

  }

}

