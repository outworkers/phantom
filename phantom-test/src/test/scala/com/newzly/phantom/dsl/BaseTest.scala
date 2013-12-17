package com.newzly.phantom.dsl

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.scalatest.{ BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec }
import com.newzly.phantom.helper.{ CassandraCluster, TestHelper}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}


object Atomics {
  val clusterStarted =  new AtomicBoolean(false)
  val startedTests = new AtomicInteger(0)
}
class BaseTest extends FlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with CassandraCluster {


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
