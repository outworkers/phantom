package com.websudos.phantom.reactivestreams.suites

import java.util.concurrent.atomic.AtomicInteger

import org.reactivestreams.{Subscription, Subscriber}
import org.scalatest.FlatSpec
import com.websudos.util.testing._
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams._

import scala.concurrent.Await
import scala.concurrent.duration._

class PublisherIntegrationTest extends FlatSpec with StreamTest with TestImplicits {

  it should "correctly consume the entire stream of items published from a Cassandra table" in {
    val counter = new AtomicInteger(0)
    val generatorCount = 100
    val samples = genList[String](generatorCount).map(Opera)

    val chain = for {
      truncate <- OperaTable.truncate().future()
      store <- samples.foldLeft(Batch.unlogged) {
        (acc, item) => {
          acc.add(OperaTable.store(item))
        }
      } future()
    } yield store

    Await.result(chain, 10.seconds)

    val publisher = OperaTable.publisher

    publisher.subscribe(new Subscriber[Opera] {
      override def onError(t: Throwable): Unit = {
        fail(t)
      }

      override def onSubscribe(s: Subscription): Unit = ()

      override def onComplete(): Unit = {
        info(s"Finished streaming, total count is ${counter.get()}")
      }

      override def onNext(t: Opera): Unit = {
        info(s"The current item is ${t.name}")
        info(s"The current count is ${counter.incrementAndGet()}")
      }
    })

  }
}
