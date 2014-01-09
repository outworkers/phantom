package com.newzly.phantom.dsl

import org.scalatest.{ BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec }
import com.newzly.phantom.helper.{ CassandraCluster, TestHelper}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import com.twitter.util.{Await, Future}
import com.twitter.conversions.time._
import com.twitter.util.{Await, Future}

object Atomics {
  val clusterStarted =  new AtomicBoolean(false)
  val startedTests = new AtomicInteger(0)
}
class BaseTest extends FlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with CassandraCluster {

  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10.seconds)
    }
  }
  override def beforeAll() {
    if (!Atomics.clusterStarted.get()) {
      if (Atomics.clusterStarted.compareAndSet(false,true)) {
        TestHelper.initCluster()
      }
    }
    Atomics.startedTests.incrementAndGet()
    while (!TestHelper.initialized) {
      Thread.sleep(1000)
    }
  }

  override def afterAll() {
    val finished = Atomics.startedTests.decrementAndGet()
    if (finished == 0) {
      cluster.shutdown()
    }
  }

  /*implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10 seconds)
    }
  }*/

}
