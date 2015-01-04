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

import java.net.InetSocketAddress

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

  val logger: Logger

  protected[zookeeper] val envString = "TEST_ZOOKEEPER_CONNECTOR"

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
    if (System.getProperty(envString) != null) {
      val inetPair: String = System.getProperty(envString)
      val split = inetPair.split(":")

      Try {
        logger.info(s"Using ZooKeeper settings from the $envString environment variable")
        logger.info(s"Connecting to ZooKeeper address: ${split(0)}:${split(1)}")
        new InetSocketAddress(split(0), split(1).toInt)
      } getOrElse {
        logger.warn(s"Failed to parse address from $envString environment variable with value: $inetPair")
        defaultAddress
      }
    } else {
      logger.info(s"No custom settings for Zookeeper found in $envString. Using localhost:2181 as default.")
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
