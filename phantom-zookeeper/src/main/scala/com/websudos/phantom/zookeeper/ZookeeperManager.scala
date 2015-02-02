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

import java.net.InetSocketAddress

import com.websudos.phantom.connectors.CassandraManager
import org.slf4j.{Logger, LoggerFactory}

import com.datastax.driver.core.{Cluster, Session}
import com.twitter.conversions.time._
import com.twitter.finagle.exp.zookeeper.ZooKeeper
import com.twitter.util.{Duration, Await, Try}

trait ZookeeperManager extends CassandraManager {

  /**
   * Interestingly enough binding to a port with a simple java.net.Socket or java.net.ServerSocket to check if a local ZooKeeper exists is not enough in this
   * day and age. We take a slightly different approach, by performing a single check when the default address is initialised. We spawn an actual ZooKeeper
   * Client using the finagle-zookeeper integration and attempt to connect. If the initial ping is successful, we conclude a ZooKeeper is found. Otherwise,
   * we conclude it doesn't exist.
   *
   * At present times the Phantom connectors are not capable of monitoring for state change system wide, e.g a move from a local ZooKeeper to an embedded and
   * so on, therefore this check can be done a single time, as any major state change in the system with regards to ZooKeeper going down would not affect
   * existing Cassandra connections and any failure in a Cassandra node is handled by the Datastax driver.
   */
  protected[this] lazy val isLocalZooKeeperRunning: Boolean = {
    Try {
      val richClient = ZooKeeper.newRichClient(s"${defaultAddress.getHostName}:${defaultAddress.getPort}")
      Await.result(richClient.connect(), 2.seconds)
    }.toOption.nonEmpty
  }

  protected[this] val store: ClusterStore

  implicit val timeout: Duration

  def cluster: Cluster = store.cluster

  def session: Session = store.session

  def logger: Logger

  protected[this] val defaultAddress = new InetSocketAddress("0.0.0.0", 2181)
}


class DefaultZookeeperManager extends ZookeeperManager {

  val livePort = 9042
  val embeddedPort = 9042

  implicit val timeout: Duration = 2.seconds

  /**
   * This is the default way a ZooKeeper connector will obtain the HOST:IP port of the ZooKeeper coordinator(master) node.
   * The phantom testing utilities are capable of auto-generating a ZooKeeper instance if none is found running.
   *
   * A test instance is ephemeral with zero persistence, it will get created, populated and deleted once per test run.
   * Upon creation, the test instance will propagate the IP:PORT combo it found available to an environment variable.
   * By convention that variable is TEST_ZOOKEEPER_CONNECTOR.
   *
   * This method will try to read that variable and parse an {@link java.net.InetSocketAddress} from it.
   * If the environment variable is null or an InetSocketAddress cannot be parsed from it, the ZooKeeper default, localhost:2181 will be used.
   * @return The InetSocketAddress of the ZooKeeper master node.
   */
  def defaultZkAddress: InetSocketAddress = if (isLocalZooKeeperRunning) {
    defaultAddress
  } else {
    if (System.getProperty(ZookeeperEnvironmentString) != null) {
      val inetPair: String = System.getProperty(ZookeeperEnvironmentString)
      val split = inetPair.split(":")

      Try {
        logger.info(s"Using ZooKeeper settings from the $ZookeeperEnvironmentString environment variable")
        logger.info(s"Connecting to ZooKeeper address: ${split(0)}:${split(1)}")
        new InetSocketAddress(split(0), split(1).toInt)
      } getOrElse {
        logger.warn(s"Failed to parse address from $ZookeeperEnvironmentString environment variable with value: $inetPair")
        defaultAddress
      }
    } else {
      logger.info(s"No custom settings for Zookeeper found in $ZookeeperEnvironmentString. Using localhost:2181 as default.")
      defaultAddress
    }
  }

  lazy val logger = LoggerFactory.getLogger("com.websudos.phantom.zookeeper")

  val store = DefaultClusterStore

  /**
   * This will initialise the Cassandra cluster connection based on the ZooKeeper connector settings.
   * It will connector to ZooKeeper, fetch the Cassandra sequence of HOST:IP pairs, and create a cluster + session for the mix.
   */
  def initIfNotInited(keySpace: String) = store.initStore(keySpace, defaultZkAddress)
}

object DefaultZookeeperManagers {
  lazy val defaultManager = new DefaultZookeeperManager
}
