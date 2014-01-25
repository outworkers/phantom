package com.newzly.phantom.helper

import java.util.concurrent.atomic.AtomicBoolean
import org.scalatest.{Assertions, Matchers, BeforeAndAfterAll, FlatSpec}
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import com.datastax.driver.core.{Session, Cluster}
import com.twitter.util.{Await, Future}

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
  lazy val cluster = BaseTestHelper.cluster
  implicit lazy val session: Session = cluster.connect()

  private[this] def createKeySpace(spaceName: String) = {
    session.execute(s"CREATE KEYSPACE $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute(s"use $spaceName;")
  }

  override def beforeAll() {
    createKeySpace(keySpace)
  }

  override def afterAll() {
    session.execute(s"DROP KEYSPACE $keySpace;")
  }

}
