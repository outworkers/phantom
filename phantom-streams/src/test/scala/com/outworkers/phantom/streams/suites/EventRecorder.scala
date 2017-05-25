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

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.duration.{ FiniteDuration, SECONDS, MILLISECONDS }

/**
  * Utility for recording events in a queue. Useful for
  * checking the ordering properties of asynchronous code. Code that does
  * stuff records an event, then the test can check that events occur in
  * the right order and at the right time.
  */
class EventRecorder(
  nextTimeout: FiniteDuration = FiniteDuration(20, SECONDS),
  isEmptyDelay: FiniteDuration = FiniteDuration(200, MILLISECONDS)
) {

  private val events = new LinkedBlockingQueue[AnyRef]

  /** Record an event. */
  def record(e: AnyRef): Boolean = events.add(e)

  /** Pull the next event, waiting up to `nextTimeout`. */
  def next(): AnyRef = {
    events.poll(nextTimeout.length, nextTimeout.unit)
  }

  /** Wait for `isEmptyDelay` then check if the event queue is empty. */
  def isEmptyAfterDelay(waitMillis: Long = 50): Boolean = {
    Thread.sleep(isEmptyDelay.toMillis)
    events.isEmpty
  }

}
