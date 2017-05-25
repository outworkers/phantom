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
import play.api.libs.iteratee._

/**
  * Creates Subscriptions that link Subscribers to an Enumerator.
  */
private[streams] trait EnumeratorSubscriptionFactory[T] extends SubscriptionFactory[T] {

  def enum: Enumerator[T]
  def emptyElement: Option[T]

  override def createSubscription[U >: T](
    subr: Subscriber[U],
    onSubscriptionEnded: SubscriptionHandle[U] => Unit
  ): EnumeratorSubscription[T, U] = {
    new EnumeratorSubscription[T, U](enum, emptyElement, subr, onSubscriptionEnded)
  }

}
