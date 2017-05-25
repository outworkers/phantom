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
  * A SubscriptionFactory is an object that knows how to create Subscriptions.
  * It can be used as a building block for creating Publishers, allowing the
  * Subscription creation logic to be factored out.
  */
private[streams] trait SubscriptionFactory[T] {

  /**
    * Create a Subscription object and return a handle to it.
    *
    * After calling this method the Subscription may be discarded, so the Subscription
    * shouldn't perform any actions or call any methods on the Subscriber
    * until `start` is called. The purpose of the `start` method is to give
    * the caller an opportunity to optimistically create Subscription objects
    * but then discard them if they can't be used for some reason. For
    * example, if two `Subscriptions` are concurrently created for the same
    * `Subscriber` then some implementations will only call `start` on
    * one of the `SubscriptionHandle`s.
    */
  def createSubscription[U >: T](
    subr: Subscriber[U],
    onSubscriptionEnded: SubscriptionHandle[U] => Unit): SubscriptionHandle[U]

}

/**
  * Wraps a Subscription created by a SubscriptionFactory, allowing the
  * Subscription to be started and queried.
  */
trait SubscriptionHandle[U] {

  /**
    * Called to start the Subscription. This will typically call the
    * onSubscribe method on the Suscription's Subscriber. In the event
    * that this method is never called the Subscription should not
    * leak resources.
    */
  def start(): Unit

  /**
    * The Subscriber for this Subscription.
    */
  def subscriber: Subscriber[U]

  /**
    * Whether or not this Subscription is active. It won't be active if it has
    * been cancelled, completed or had an error.
    */
  def isActive: Boolean
}

