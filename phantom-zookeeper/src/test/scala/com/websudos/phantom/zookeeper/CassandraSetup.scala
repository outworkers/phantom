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
package com.websudos.phantom.zookeeper

import java.io.IOException
import java.net.Socket

import com.twitter.util.NonFatal
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

import scala.concurrent._


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


private[zookeeper] object ZookeeperManager {
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
