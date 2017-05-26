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

import com.outworkers.phantom.streams.lib.EnumeratorPublisher
import org.reactivestreams._
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.iteratee.{Concurrent, Enumerator, Input}

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.control.NonFatal

class EnumeratorPublisherSpec extends FlatSpec with Matchers {

  case object OnSubscribe
  case class OnError(t: Throwable)
  case class OnNext(element: Any)
  case object OnComplete
  case class RequestMore(elementCount: Int)
  case object Cancel
  case object GetSubscription

  class TestEnv[T] extends EventRecorder() {

    object Subscriber extends Subscriber[T] {
      val subscription = Promise[Subscription]()
      override def onSubscribe(s: Subscription): Unit = {
        record(OnSubscribe)
        subscription.success(s)
      }
      override def onError(t: Throwable): Unit = record(OnError(t))
      override def onNext(element: T): Unit = record(OnNext(element))
      override def onComplete(): Unit = record(OnComplete)
    }

    def forSubscription(f: Subscription => Any): Future[Unit] = {
      Subscriber.subscription.future.map(f).map(_ => ())
    }
    def request(elementCount: Int): Future[Unit] = {
      forSubscription { s =>
        record(RequestMore(elementCount))
        s.request(elementCount)
      }
    }
    def cancel(): Future[Unit] = {
      forSubscription { s =>
        record(Cancel)
        s.cancel()
      }
    }

  }

  it should "enumerate one item" in {
    val testEnv = new TestEnv[Int]
    val enum = Enumerator(1) >>> Enumerator.eof
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnNext(1)
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "enumerate three items, with batched requests" in {
    val testEnv = new TestEnv[Int]
    val enum = Enumerator(1, 2, 3) >>> Enumerator.eof
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(2)
    testEnv.next shouldEqual RequestMore(2)
    testEnv.next shouldEqual OnNext(1)
    testEnv.next shouldEqual OnNext(2)
    testEnv.request(2)
    testEnv.next shouldEqual RequestMore(2)
    testEnv.next shouldEqual OnNext(3)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "be done enumerating after EOF" in {
    val size = 4
    val testEnv = new TestEnv[Int]
    val enumDone = Promise[Boolean]()
    val enum = (Enumerator(1, 2, 3) >>> Enumerator.eof).onDoneEnumerating {
      enumDone.success(true)
    }
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(size)
    testEnv.next shouldEqual RequestMore(size)
    testEnv.next shouldEqual OnNext(1)
    testEnv.next shouldEqual OnNext(2)
    testEnv.next shouldEqual OnNext(3)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
    Await.result(enumDone.future, 10.seconds) shouldBe true
  }

  it should "complete the subscriber when done enumerating without eof" in {
    val size = 4
    val testEnv = new TestEnv[Int]
    val enum = Enumerator(1, 2, 3)
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(size)
    testEnv.next shouldEqual RequestMore(size)
    testEnv.next shouldEqual OnNext(1)
    testEnv.next shouldEqual OnNext(2)
    testEnv.next shouldEqual OnNext(3)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "be done enumerating after being cancelled" in {
    val size = 4
    val testEnv = new TestEnv[Int]
    val enumDone = Promise[Boolean]()
    val (broadcastEnum, channel) = Concurrent.broadcast[Int]
    val enum = broadcastEnum.onDoneEnumerating {
      enumDone.success(true)
    }
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(size)
    testEnv.next shouldEqual RequestMore(size)
    testEnv.isEmptyAfterDelay() shouldBe true
    testEnv.cancel()
    testEnv.next shouldEqual Cancel
    // Element push occurs after cancel, so will not generate an event.
    // However it is necessary to have an event so that the publisher's
    // Cont is satisfied. We want to advance the iteratee to pick up the
    // Done iteratee caused by the cancel.
    Try {
      channel.push(0)
      Await.result(enumDone.future, 10.seconds) shouldBe true
    } recover {
      case NonFatal(t) =>
        // If it didn't work the first time, try again, since cancel only guarantees that the publisher will
        // eventually finish
        channel.push(0)
        Await.result(enumDone.future, 10.seconds) shouldBe true

      case e @ _ => throw e
    }
  }

  it should "enumerate eof only" in {
    val testEnv = new TestEnv[Int]
    val enum: Enumerator[Int] = Enumerator.eof
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "by default, enumerate nothing for empty" in {
    val testEnv = new TestEnv[Int]
    val enum: Enumerator[Int] = Enumerator.enumInput(Input.Empty) >>> Enumerator.eof
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "be able to enumerate something for empty" in {
    val testEnv = new TestEnv[Int]
    val enum: Enumerator[Int] = Enumerator.enumInput(Input.Empty) >>> Enumerator.eof
    val pubr = new EnumeratorPublisher(enum, emptyElement = Some(-1))
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnNext(-1)
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "handle errors when enumerating" in {
    val testEnv = new TestEnv[Int]
    val exception = new Exception("x")
    val enum = Enumerator.flatten(Future.failed(exception))
    val pubr = new EnumeratorPublisher[Nothing](enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next() match {
      case OnError(e) => e.getMessage shouldEqual exception.getMessage
      case _ => fail("Expecting error to happen, got a different item in the publisher")
    }
    testEnv.isEmptyAfterDelay() shouldBe true
  }

  it should "enumerate 25 items" in {
    val testEnv = new TestEnv[Int]
    val lotsOfItems = 0 until 25
    val enum = Enumerator(lotsOfItems: _*) >>> Enumerator.eof
    val pubr = new EnumeratorPublisher(enum)
    pubr.subscribe(testEnv.Subscriber)
    testEnv.next shouldEqual OnSubscribe
    for (i <- lotsOfItems) {
      testEnv.request(1)
      testEnv.next shouldEqual RequestMore(1)
      testEnv.next shouldEqual OnNext(i)
    }
    testEnv.request(1)
    testEnv.next shouldEqual RequestMore(1)
    testEnv.next shouldEqual OnComplete
    testEnv.isEmptyAfterDelay() shouldBe true
  }

}