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
package com.outworkers.phantom.streams.lib

import org.reactivestreams._

/**
  * A Publisher which subscribes Subscribers without performing any
  * checking about whether he Suscriber is already subscribed. This
  * makes RelaxedPublisher a bit faster, but possibly a bit less safe,
  * than a CheckingPublisher.
  */
private[streams] abstract class RelaxedPublisher[T] extends Publisher[T] {
  self: SubscriptionFactory[T] =>

  // Streams method
  final override def subscribe(subr: Subscriber[_ >: T]): Unit = {
    val handle: SubscriptionHandle[_] = createSubscription(subr, RelaxedPublisher.onSubscriptionEndedNop)
    handle.start()
  }

}

private[streams] object RelaxedPublisher {
  val onSubscriptionEndedNop: Any => Unit = _ => ()
}
