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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, blocking}

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}

import com.datastax.driver.core.Session
import com.twitter.util.NonFatal
import com.websudos.phantom.zookeeper.{DefaultZookeeperConnector, ZookeeperInstance}


private[testing] object CassandraStateManager {

  private[this] def isPortAvailable(port: Int): Boolean = {
    try {
      new ServerSocket(port)
      Console.println(s"$port available")
      true
    } catch {
      case ex: IOException => {
        Console.println(ex.getMessage)
        Console.println(s"$port unavailable")
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

  /**
   * This checks if the default ports for embedded Cassandra and
   * @return
   */
  def isCassandraStarted: Boolean = {
    isLocalCassandraRunning
  }
}


private[testing] object ZookeperManager {
  lazy val zkInstance = new ZookeeperInstance()

  private[this] var isStarted = false

  def start(): Unit = {
    if (!isStarted) {
      zkInstance.start()
      isStarted = true
    }
  }
}

private[testing] object Lock

trait CassandraSetup {

  def setupCassandra(): Unit = {
    Lock.synchronized {
      blocking {
        if (!CassandraStateManager.isCassandraStarted) {
          try {
            Console.println("Starting cassandra")
            EmbeddedCassandraServerHelper.mkdirs()
          } catch {
            case NonFatal(e) => println(e.getMessage)
          }
          EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml")
        } else {
            Console.println("Cassandra already running")
        }
      }
    }
  }

}

trait TestZookeeperConnector extends DefaultZookeeperConnector with CassandraSetup {
  val keySpace = "phantom"
  ZookeperManager.start()

}

trait CassandraTest extends ScalaFutures with Matchers with Assertions with AsyncAssertions with CassandraSetup with BeforeAndAfterAll {

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


