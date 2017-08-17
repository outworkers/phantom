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

import com.outworkers.phantom.builder.batch.BatchType
import com.outworkers.phantom.streams.BatchSubscriber
import com.outworkers.util.samplers._
import org.reactivestreams.tck.SubscriberWhiteboxVerification.{SubscriberPuppet, WhiteboxSubscriberProbe}
import org.reactivestreams.tck.{SubscriberWhiteboxVerification, TestEnvironment}
import org.reactivestreams.{Subscriber, Subscription}

class BatchSubscriberWhiteboxTest extends SubscriberWhiteboxVerification[Opera](new TestEnvironment())
  with StreamDatabase.connector.Connector with TestImplicits {

  override def createSubscriber(probe: WhiteboxSubscriberProbe[Opera]): Subscriber[Opera] = {
    new BatchSubscriber[OperaTable, Opera](
      StreamDatabase.operaTable,
      builder = OperaRequestBuilder,
      batchSize = 5,
      concurrentRequests = 2,
      batchType = BatchType.Unlogged,
      flushInterval = None,
      completionFn = () => (),
      errorFn = _ => ()
    ) {

      override def onSubscribe(s: Subscription): Unit = {
        super.onSubscribe(s)

        probe.registerOnSubscribe(new SubscriberPuppet {

          override def triggerRequest(elements: Long): Unit = {
            s.request(elements)
          }

          override def signalCancel(): Unit = {
            s.cancel()
          }
        })
      }

      override def onComplete(): Unit = {
        super.onComplete()
        probe.registerOnComplete()
      }

      override def onError(t: Throwable): Unit = {
        probe.registerOnError(t)
      }

      override def onNext(t: Opera): Unit = {
        super.onNext(t)
        probe.registerOnNext(t)
      }

    }
  }

  override def createElement(element: Int): Opera = Opera(gen[String])
}