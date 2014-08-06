/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.testing

import java.util.concurrent.atomic.AtomicBoolean

import scala.concurrent.blocking

import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.{Assertions, BeforeAndAfterAll, FeatureSpec, FlatSpec, Matchers, Suite}

import com.datastax.driver.core.{Cluster, Session}
import com.websudos.phantom.zookeeper.CassandraConnector

trait CassandraManager {
  val cluster: Cluster
  implicit def session: Session
}

object DefaultCassandraManager extends CassandraManager {

  private[this] val inited = new AtomicBoolean(false)
  @volatile private[this] var _session: Session = null

  lazy val cluster: Cluster = Cluster.builder()
    .addContactPoint("localhost")
    .withPort(9142)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  def session = _session

  def initIfNotInited(keySpace: String): Unit = {
    if (inited.compareAndSet(false, true)) {
      _session = blocking {
          val s = cluster.connect()
          s.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
          s.execute(s"USE $keySpace;")
          s
        }
    }
  }

}


trait SimpleCassandraConnector extends CassandraSetup with CassandraConnector {
  implicit lazy val session: Session = DefaultCassandraManager.session
}

trait SimpleCassandraTest extends ScalaFutures with SimpleCassandraConnector with Matchers with Assertions with AsyncAssertions with BeforeAndAfterAll {
  self : BeforeAndAfterAll with Suite =>

  override def beforeAll() {
    super.beforeAll()
    setupCassandra()
    DefaultCassandraManager.initIfNotInited(keySpace)
  }
}

trait CassandraFlatSpec extends FlatSpec with SimpleCassandraTest
trait CassandraFeatureSpec extends FeatureSpec with SimpleCassandraTest


trait PhantomCassandraConnector extends SimpleCassandraConnector {
  val keySpace = "phantom"
}

trait PhantomCassandraTestSuite extends CassandraFlatSpec with PhantomCassandraConnector
