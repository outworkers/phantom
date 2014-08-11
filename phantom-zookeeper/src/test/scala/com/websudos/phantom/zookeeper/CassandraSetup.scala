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

package com.websudos.phantom.zookeeper

import java.io.IOException
import java.net.Socket

import scala.concurrent._

import org.cassandraunit.utils.EmbeddedCassandraServerHelper

import com.twitter.util.NonFatal


/**
 * This is a duplicate implementation of
 */
private[zookeeper] object CassandraStateManager {

  private[this] def isPortAvailable(port: Int): Boolean = {
    try {
      new Socket("localhost", port)
      true
    } catch  {
      case ex: IOException => false
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

  def isCassandraStarted: Boolean = {
    isEmbeddedCassandraRunning || isLocalCassandraRunning
  }
}


private[zookeeper] object ZookeperManager {
  lazy val zkInstance = new ZookeeperInstance()

  private[this] var isStarted = false

  def start(): Unit = {
    if (!isStarted) {
      zkInstance.start()
      isStarted = true
    }
  }
}

private[zookeeper] object CassandraLock

private[zookeeper] trait CassandraSetup {

  def setupCassandra(): Unit = {
    CassandraLock.synchronized {
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
          Console.println("Cassandra is already running")
        }
      }
    }
  }

}
