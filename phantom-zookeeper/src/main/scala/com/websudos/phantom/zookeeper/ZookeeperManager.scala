/*
 *
 *  * Copyright 2014 newzly ltd.
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
import java.net.{InetSocketAddress, Socket}

import org.slf4j.{Logger, LoggerFactory}

import com.datastax.driver.core.{Cluster, Session}

trait ZookeeperManager extends CassandraManager {

  protected[this] def store: ClusterStore

  def cluster: Cluster = store.cluster

  def session: Session = store.session

  def logger: Logger

  protected[zookeeper] def envString = "TEST_ZOOKEEPER_CONNECTOR"

  protected[this] val defaultAddress = new InetSocketAddress("localhost", 2181)
}



class DefaultZookeeperManager extends ZookeeperManager {

  private[this] def isPortAvailable(port: Int): Boolean = {
    try {
      new Socket("localhost", port)
      true
    } catch  {
      case ex: IOException => false
    }
  }

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
  def defaultZkAddress: InetSocketAddress = {
      defaultAddress
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
