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
package com.outworkers.phantom.streams.suites.iteratee

import java.util.concurrent.atomic.AtomicInteger

import com.outworkers.phantom.streams.suites.{Opera, OperaData}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

object OperaPublisher extends Publisher[Opera] {

  val counter = new AtomicInteger(0)

  override def subscribe(s: Subscriber[_ >: Opera]): Unit = {
    s.onSubscribe(new Subscription {
      override def cancel(): Unit = ()

      override def request(l: Long): Unit = {
        val start = counter.getAndIncrement() * l.toInt
        val end = start + l.toInt

        if (start < OperaData.operas.size) {
          OperaData.operas.slice(start, end).foreach(i => s.onNext(i))
        } else {
          s.onComplete()
        }
      }
    })
  }

}
