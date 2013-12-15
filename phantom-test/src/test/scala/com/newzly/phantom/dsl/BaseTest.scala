package com.newzly.phantom.dsl

import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.scalatest.{ BeforeAndAfterEach, BeforeAndAfterAll, FlatSpec }
import com.newzly.phantom.helper.{ CassandraCluster, TestHelper}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

class BaseTest extends FlatSpec with BeforeAndAfterEach with BeforeAndAfterAll with CassandraCluster {

  override def beforeAll() {
    TestHelper.initCluster
  }

  override def afterAll() {
    cluster.shutdown()
  }

  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10 seconds)
    }
  }

}
