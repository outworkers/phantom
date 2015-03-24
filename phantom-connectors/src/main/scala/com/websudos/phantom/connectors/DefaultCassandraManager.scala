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
package com.websudos.phantom.connectors

import java.io.IOException
import java.net.Socket

import com.datastax.driver.core.exceptions.{DriverInternalError, NoHostAvailableException}
import com.datastax.driver.core.{Cluster, Session}

import scala.concurrent.blocking
import scala.util.control.NonFatal

trait DefaultCassandraManager extends CassandraManager {

  val livePort = 9042
  val embeddedPort = 9142
  def cassandraHost: String = "localhost"

  private[this] var inited = false
  private[this] var _session: Session = null

  private[this] def shouldAttemptReconnect(exception: Throwable): Boolean = {
    exception match {
      case e: NoHostAvailableException => false
      case f: DriverInternalError => false
      case _ => true
    }
  }

  def clusterRef: Cluster = {
    if (cluster.isClosed) {
      try {
        blocking {
          cluster.connect()
        }
        cluster
      } catch {
        case NonFatal(e) => {
          if (shouldAttemptReconnect(e)) {
            createCluster()
          } else {
            throw new Exception("Unable to recreate cluster connection. Cluster is unavailable", e)
          }
        }
      }
    } else {
      cluster
    }
  }

  def cassandraPort: Int = {
    try {
      new Socket(cassandraHost, livePort)
      livePort
    } catch {
      case ex: IOException => embeddedPort
    }
  }

  /**
   * This method tells the manager how to create a Cassandra cluster out of the provided settings.
   * It deals with the underlying Datastax Cluster builder with a set of defaults that can be easily overridden.
   *
   * The purpose of this method, beyond DRY, is to allow users to override the building of a cluster with whatever they need.
   * @return A reference to a Cassandra/DSE cluster.
   */
  protected[this] def createCluster(): Cluster = {
    Cluster.builder()
      .addContactPoint(cassandraHost)
      .withPort(cassandraPort)
      .withoutJMXReporting()
      .withoutMetrics()
      .build()
  }

  lazy val cluster = createCluster()

  def session = CassandraInitLock.synchronized { _session }


  def initIfNotInited(keySpace: String): Unit = CassandraInitLock.synchronized {
    if (!inited) {
      _session = blocking {
        val s = clusterRef.connect()
        s.execute(createKeySpace(keySpace))
        s
      }
      inited = true
    }
  }
}

object DefaultCassandraManager extends DefaultCassandraManager
