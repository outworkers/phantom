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

import java.io.IOException
import java.net.ServerSocket

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, blocking}

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}

import com.datastax.driver.core.Session
import com.twitter.util.NonFatal
import com.websudos.phantom.zookeeper.{DefaultZookeeperConnector, ZookeeperInstance}


private[testing] object CassandraStateManager {

  val logger = LoggerFactory.getLogger("com.websudos.phantom.testing")

  private[this] def isPortAvailable(port: Int): Boolean = {
    try {
      new ServerSocket(port)
      logger.info(s"Port $port available")
      true
    } catch {
      case ex: IOException => {
        logger.info(s"Port $port not available")
        false
      }
    }
  }

  /**
   * This does a dummy check to see if Cassandra is started.
   * It checks for default ports for embedded Cassandra and local Cassandra.
   * @return A boolean saying if Cassandra is started.
   */
  def isEmbeddedCassandraRunning: Boolean = {
    !isPortAvailable(9142)
  }

  def isLocalCassandraRunning: Boolean = {
    !isPortAvailable(9042)
  }

  def cassandraRunning(): Boolean = {
    try {
      val runtime = Runtime.getRuntime

      val p1 = runtime.exec("ps -ef")
      val input = p1.getInputStream

      val p2 = runtime.exec("grep cassandra")
      val output = p2.getOutputStream

      IOUtils.copy(input, output)
      output.close(); // signals grep to finish
      val result = IOUtils.readLines(p2.getInputStream)
      result.size() > 1
    } catch  {
      case NonFatal(e) => false
    }
  }


  /**
   * This checks if the default ports for embedded Cassandra and local Cassandra.
   * @return
   */
  def isCassandraStarted: Boolean = {
    !isPortAvailable(9042) || !isPortAvailable(9142)
  }
}


private[testing] object ZooKeeperManager {
  lazy val zkInstance = new ZookeeperInstance()

  private[this] var isStarted = false

  def start(): Unit = Lock.synchronized {
    if (!isStarted) {
      zkInstance.start()
      isStarted = true
    }
  }
}

private[testing] object Lock

trait CassandraSetup {

  /**
   * This method tries to check if a local Cassandra instance is found and if not start an embedded version.
   * For the time being, the detection mechanism is not completely reliable as we have yet to reach the sweet spot of killing Cassandra and stale JVMs and we
   * cannot also reliably detect a running Cassandra cluster using the above methods.
   *
   * This improved method (in 1.4.1) will try to perform both a port and process check before starting Cassandra in embedded mode.
   */
  def setupCassandra(): Unit = {
    Lock.synchronized {
      blocking {
        if (!(CassandraStateManager.cassandraRunning() || CassandraStateManager.isCassandraStarted)) {
          try {
            CassandraStateManager.logger.info("Starting Cassandra in Embedded mode.")
            EmbeddedCassandraServerHelper.mkdirs()
          } catch {
            case NonFatal(e) => {
              CassandraStateManager.logger.error(e.getMessage)
            }
          }
          EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml")
        } else {
          CassandraStateManager.logger.info("Cassandra is already running.")
        }
      }
    }
  }

}

trait TestZookeeperConnector extends DefaultZookeeperConnector with CassandraSetup {
  val keySpace = "phantom"
  ZooKeeperManager.start()

}

trait CassandraTest extends ScalaFutures
  with Matchers with Assertions
  with AsyncAssertions with CassandraSetup
  with BeforeAndAfterAll {

  self : BeforeAndAfterAll with Suite =>

  implicit def session: Session
  implicit lazy val context: ExecutionContext = global

  override def beforeAll() {
    super.beforeAll()
    setupCassandra()
  }
}

trait BaseTest extends FlatSpec with CassandraTest with TestZookeeperConnector

trait FeatureBaseTest extends FeatureSpec with CassandraTest with TestZookeeperConnector


