package com.newzly.phantom.helper

import scala.concurrent.ExecutionContext
import org.scalatest.{ Assertions, BeforeAndAfterAll, FlatSpec, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }
import com.datastax.driver.core.{ Cluster, Session }
import com.newzly.phantom.Manager

object BaseTestHelper {
  val cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9142)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

}

trait BaseTest extends FlatSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions {
  val keySpace: String
  val cluster = BaseTestHelper.cluster
  implicit lazy val session: Session = cluster.connect()
  implicit lazy val context: ExecutionContext = Manager.scalaExecutor


  private[this] def createKeySpace(spaceName: String) = {
    session.execute(s"CREATE KEYSPACE IF NOT EXISTS $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute(s"use $spaceName;")
  }

  override def beforeAll() {
    createKeySpace(keySpace)
  }

  override def afterAll() {
    session.execute(s"DROP KEYSPACE $keySpace;")
  }

}