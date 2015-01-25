/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.testing

import java.io.IOException
import java.net.ServerSocket

import com.datastax.driver.core.{Cluster, Session}
import com.twitter.util.NonFatal
import com.websudos.phantom.zookeeper.{DefaultZookeeperConnector, ZookeeperInstance}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, blocking}
import scala.util.Try


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

  def checkRunningCassandra(host: String, port: Int): Boolean = {
    Try {
      val cluster = Cluster.builder()
        .addContactPoint(host)
        .withPort(port)
        .withoutJMXReporting()
        .withoutMetrics()
        .build()

      blocking {
        cluster.connect()
        true

      }
    } getOrElse false
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
        if (!(CassandraStateManager.checkRunningCassandra("localhost", 9042) || CassandraStateManager.checkRunningCassandra("localhost", 9142))) {
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


